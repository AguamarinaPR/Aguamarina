package cm.aptoide.pt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread.State;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

public class FetchExtrasService extends Service{

	private final String SDCARD = Environment.getExternalStorageDirectory().getPath();
	private String LOCAL_PATH = SDCARD+"/.aptoide";
	private String REMOTE_EXTRAS_FILE = "/extras.xml";
	private String EXTRAS_XML_PATH = LOCAL_PATH+"/extras.xml";
	
	private List<ServerNode> parsedList = null;
	private List<ServerNode> retryList = null;
	private Thread workingPool = null;
	Context mctx = null;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		
		mctx = this;
		
		parsedList = new ArrayList<ServerNode>();
		retryList = new ArrayList<ServerNode>();
		workingPool = new Thread(new WorkThread(), "T1");
		workingPool.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
	}

		
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Bundle intent_info = intent.getExtras();
		
		if(workingPool.getState() == State.NEW){
			parsedList = (ArrayList<ServerNode>) intent_info.getSerializable("lstex");
			workingPool.start();
			Log.d("Aptoide","........................Extras service starting....");
		}
	}



	private class WorkThread implements Runnable {
		public WorkThread() {	}
		
		public void run() {
			try{
				for(ServerNode node: parsedList){
					if(node.inuse){
						Log.d("Aptoide", "-------------------------Extras for: " + node.uri);
						try{
							if(downloadExtras(node.uri, node.hash)){
								xmlPass(node.uri);
							}else{
								retryList.add(node);
							}
						}catch(Exception e ) {retryList.add(node);}
					}
				}
				for(ServerNode node : retryList){
					if(node.inuse){
						try{
							if(downloadExtras(node.uri, node.hash)){
								xmlPass(node.uri);
							}
						}catch(Exception e ) {}
					}
				}
				Log.d("Aptoide","......................Extras service STOPING....");
				stopSelf();
			}catch (Exception e){ 	}
		}
	}
	
	private void xmlPass(String srv){
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    File xml_file = null;
	    SAXParser sp = null;
    	XMLReader xr = null;
    	try {
    		sp = spf.newSAXParser();
    		xr = sp.getXMLReader();

    		ExtrasRssHandler handler = new ExtrasRssHandler(this, srv);
    		xr.setContentHandler(handler);
    		xml_file = new File(EXTRAS_XML_PATH);


    		InputStreamReader isr = new FileReader(xml_file);
    		InputSource is = new InputSource(isr);
    		xr.parse(is);

    	} catch (Exception e){
    		xr = null;
    	}finally{
    		xml_file.delete();
    	}
	}
	
	
	/*
	 * Get extras.xml file from server and save it in the SD card 
	 */
	private boolean downloadExtras(String srv, String delta_hash){
		String url = srv+REMOTE_EXTRAS_FILE;
		
        try {
        	//String delta_hash = db.getServerDelta(srv);
        	if(delta_hash.length()>2)
        		url = url.concat("?hash="+delta_hash);
        	
        	Log.d("Aptoide","A fazer fetch extras de: " + url);

        	
        	FileOutputStream saveit = new FileOutputStream(LOCAL_PATH+REMOTE_EXTRAS_FILE);

        	HttpResponse mHttpResponse = NetworkApis.getHttpResponse(url, srv, mctx);
        	
			if(mHttpResponse.getStatusLine().getStatusCode() == 200){
				
				Log.d("Aptoide","extras.xml: " + mHttpResponse.getEntity().getContentEncoding());
//@dsilveira #531 +20lines fix OutOfMemory crash				
				InputStream instream = null;
				
				if((mHttpResponse.getEntity().getContentEncoding() != null) && (mHttpResponse.getEntity().getContentEncoding().getValue().equalsIgnoreCase("gzip"))){

					Log.d("Aptoide","with gzip");
					instream = new GZIPInputStream(mHttpResponse.getEntity().getContent());
					
				}else{

					Log.d("Aptoide","No gzip");
					instream = mHttpResponse.getEntity().getContent();
					
				}
				
				int nRead;
				byte[] data = new byte[1024];

				while ((nRead = instream.read(data, 0, data.length)) != -1) {
					saveit.write(data, 0, nRead);
				}
				
			}else{
				return false;
				//Does nothing...
			}
			return true;
		} catch (UnknownHostException e){ return false;} 
		  catch (ClientProtocolException e) { return false;} 
		  catch (IOException e) { return false;}
		  catch (Exception e) {return false;	}
	}
	
	
	
	
}
