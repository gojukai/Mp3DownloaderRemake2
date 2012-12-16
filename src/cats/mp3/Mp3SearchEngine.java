package cats.mp3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cats.mp3.event.DownloadEvent;
import cats.mp3.event.DownloadListener;

public class Mp3SearchEngine{
	
	public static final String BASE_URL = "http://www.mp3skull.com";
	public static final int DEFAULT_TIMEOUT = 60000;
	
	private int timeout;
	
	private File output;
	
	protected ExecutorService service;
	
	private LinkedList<DownloadListener> listeners;
	
	public Mp3SearchEngine(final int timeout, final int threadLimit){
		this.timeout = timeout;
		service = Executors.newFixedThreadPool(threadLimit);
		
		listeners = new LinkedList<DownloadListener>();
		
		output = new File(System.getProperty("user.home"));
	}
	
	public Mp3SearchEngine(final int threadLimit){
		this(DEFAULT_TIMEOUT, threadLimit);
	}
	
	public void addDownloadListener(final DownloadListener dl){
		listeners.add(dl);
	}
	
	public void removeDownloadListener(final DownloadListener dl){
		listeners.remove(dl);
	}
	
	protected void fireDownloadStartEvents(final DownloadEvent e){
		for(final DownloadListener dl : listeners)
			dl.onStart(e);
	}
	
	protected void fireDownloadFinishEvents(final DownloadEvent e){
		for(final DownloadListener dl : listeners)
			dl.onFinish(e);
	}
	
	protected void fireDownloadUpdateEvents(final DownloadEvent e){
		for(final DownloadListener dl : listeners)
			dl.onUpdate(e);
	}
	
	public void setDestinationDirectory(final File dir){
		this.output = dir;
	}
	
	public File getDestinationDirectory(){
		return output;
	}
	
	public int getTimeout(){
		return timeout;
	}
	
	public void setTimeout(final int timeout){
		this.timeout = timeout;
	}
	
	public void shutdown(final boolean immediately){
		if(immediately)
			service.shutdownNow();
		else
			service.shutdown();
	}
	
	public void shutdown(){
		shutdown(false);
	}
	
	public List<Mp3> search(final String search) throws IOException{
		final List<Mp3> list = new LinkedList<Mp3>();
		final URL url = new URL(String.format("http://mp3skull.com/mp3/%s.html", URLEncoder.encode(search, "UTF-8")));
		final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setUseCaches(false);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setReadTimeout(timeout);
		connection.setConnectTimeout(timeout);
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1309.0 Safari/537.17");
		connection.addRequestProperty("User-Content", "application/x-www-form-urlencoded");
		connection.setRequestMethod("POST");
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
		writer.write("ord=br2");
		writer.flush();
		writer.close();
		String[] split = null;
		String bitrate = null;
		String duration = null;
		String size = null;
		String fileName = null;
		String downloadLink = null;
		final Scanner reader = new Scanner(connection.getInputStream());
		while(reader.hasNextLine()){
			String line = reader.nextLine().trim();
			if(line.contains("<!-- info mp3 here -->")){
				line = reader.nextLine().trim();
				split = line.split("<br />");
				if(split.length != 3) continue;
				bitrate = split[0].trim();
				duration = split[1].trim();
				size = split[2].split("</div>")[0].trim();
				while(!(line = reader.nextLine().trim()).contains("<div style=\"font-size:15px;\">"));
				fileName = line.split("<b>")[1].split("</b>")[0].trim();
				while(!(line = reader.nextLine().trim()).contains("style=\"color:green;\">Download</a></div>"));
				downloadLink = line.split("\"")[3].trim();
				list.add(new Mp3(this, fileName, bitrate, duration, size, downloadLink));
				split = null;
				bitrate = null;
				duration = null;
				size = null;
				fileName = null;
			}
		}
		reader.close();
		return Collections.unmodifiableList(list);
	}

}
