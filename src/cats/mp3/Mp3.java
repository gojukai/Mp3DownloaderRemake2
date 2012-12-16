package cats.mp3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import cats.mp3.event.DownloadEvent;

public class Mp3{
	
	private Mp3SearchEngine engine;
	
	private String fileName;
	
	private String size;
	private String bitrate;
	private String duration;
	
	private String downloadLink;
	
	private Runnable downloader;
			
	public Mp3(final Mp3SearchEngine engine, final String fileName, final String bitrate, final String duration, final String size, final String downloadLink){
		this.engine = engine;
		this.fileName = fileName.replaceAll("&#039;", "'").replaceAll("%20", " ").replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll(File.pathSeparator, "-");
		if(this.fileName.contains(" mp3")) this.fileName = fileName.substring(0, fileName.lastIndexOf(" mp3"));
		if(!this.fileName.endsWith(".mp3")) this.fileName += ".mp3";
				
		this.bitrate = bitrate;
		this.duration = duration;
		this.size = size;
		
		this.downloadLink = downloadLink;
		
		downloader = new Runnable(){
			public void run(){
				InputStream input = null;
				FileOutputStream output = null;
				double total = 0;
				double bytesRead = 0;
				int singleRead = 0;
				byte[] buffer = new byte[15600];
				File out = new File(engine.getDestinationDirectory(), Mp3.this.fileName);
				try{
					final URL url = new URL(downloadLink);
					final URLConnection connection = url.openConnection();
					connection.setUseCaches(false);
					connection.setReadTimeout(engine.getTimeout());
					connection.setConnectTimeout(engine.getTimeout());
					connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1309.0 Safari/537.17");
					connection.addRequestProperty("User-Content", "application/x-www-form-urlencoded");
					input = connection.getInputStream();
					output = new FileOutputStream(out);
					total = connection.getContentLength();
					engine.fireDownloadStartEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
					engine.fireDownloadUpdateEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
					while((singleRead = input.read(buffer)) > 0){
						output.write(buffer, 0, singleRead);
						bytesRead += singleRead;
						engine.fireDownloadUpdateEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
					}
					input.close();
					output.close();
					engine.fireDownloadFinishEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, false));
				}catch(IOException e){
					engine.fireDownloadFinishEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, true));
				}finally{
					try{
					if(input != null) 
						input.close();
					if(output != null)
						output.close();
					}catch(IOException e){
						engine.fireDownloadFinishEvents(new DownloadEvent(Mp3.this, bytesRead, total, out, true));
					}
				}
			}
		};
	}
	
	public void download(){
		engine.service.execute(downloader);
	}
	
	public boolean isDownloadable(){
		return downloadLink != null;
	}
	
	public boolean canDownload(){
		return isDownloadable() && engine.getDestinationDirectory() != null;
	}
	
	public Mp3SearchEngine getEngine(){
		return engine;
	}
	
	public String getSize(){
		return size;
	}
	
	public String getBitrate(){
		return bitrate;
	}
	
	public String getDuration(){
		return duration;
	}
	
	public String getDownloadLink(){
		return downloadLink;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public String toString(){
		return String.format("Name: %s | Size: %s | Bitrate: %s | Duration: %s | Download: %s", fileName, size, bitrate, duration, downloadLink);
	}
	
}
