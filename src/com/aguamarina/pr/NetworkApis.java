/* Copyright (C) 2023-2025 TropicalBananas */
/* Cookie challenge solving contributed by PBLinuxMintDE6 in 2026. */

package com.aguamarina.pr;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.SharedPreferences;

public class NetworkApis {

	private static final String terminal_info = android.os.Build.MODEL + "("+ android.os.Build.PRODUCT + ")"
	+";v"+android.os.Build.VERSION.RELEASE+";"+System.getProperty("os.arch");
	
	private static String cookie = "";
	private static boolean solvingChallenge = false;
	private static Pattern p = Pattern.compile(
			"var\\s+a=toNumbers\\(\"([0-9a-f]+)\"\\),\\s*" +
					"b=toNumbers\\(\"([0-9a-f]+)\"\\),\\s*" +
					"c=toNumbers\\(\"([0-9a-f]+)\"\\)"
	);

	private static String readStream(java.io.InputStream in) throws Exception {
		java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(in, "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = r.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	private static byte[] hexToBytes(String s) {
		int len = s.length();
		byte[] out = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			out[i / 2] = (byte)(
					(Character.digit(s.charAt(i), 16) << 4)
							+ Character.digit(s.charAt(i + 1), 16)
			);
		}
		return out;
	}

	private static String bytesToHex(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) sb.append('0');
			sb.append(Integer.toHexString(v));
		}
		return sb.toString();
	}
	
	public static HttpResponse getHttpResponse(String url, String srv, Context mctx){
		try{
			DbHandler db = new DbHandler(mctx);
			
			SharedPreferences sPref = mctx.getSharedPreferences("aguamarina_prefs", Context.MODE_PRIVATE);
			String myub = sPref.getString("myUB", "NoInfo");
			String myscr = sPref.getInt("scW", 0)+"x"+sPref.getInt("scH", 0);
						
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 12000);
			HttpConnectionParams.setSoTimeout(httpParameters, 12000);
			
			DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
			mHttpClient.setRedirectHandler(new RedirectHandler() {

				public boolean isRedirectRequested(HttpResponse response,
						HttpContext context) {
					return false;
				}
				

				public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
					return null;
				}
			});
			
			
			HttpGet mHttpGet = new HttpGet(url);
			mHttpGet.setHeader("User-Agent", "aguamarina-" + mctx.getString(R.string.ver_str)+";"+ terminal_info+";"+myscr+";id:"+myub);
			mHttpGet.setHeader("Accept-Encoding", "gzip");
			mHttpGet.setHeader("Cookie", cookie);
			
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
			
			Header ct = mHttpResponse.getFirstHeader("Content-Type");
			if (!solvingChallenge && ct != null &&
					ct.getValue().startsWith("text/html")) {

				String html = readStream(mHttpResponse.getEntity().getContent());

				Matcher m = p.matcher(html);
				if (m.find()) {
					solvingChallenge = true;

					String aHex = m.group(1);
					String bHex = m.group(2);
					String cHex = m.group(3);

					byte[] key = hexToBytes(aHex);
					byte[] iv = hexToBytes(bHex);
					byte[] ctf = hexToBytes(cHex);

					Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

					cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

					byte[] plain = cipher.doFinal(ctf);
					cookie = "__test=" + bytesToHex(plain);

					mHttpGet = new HttpGet(url);
					mHttpGet.setHeader("User-Agent", "aguamarina-" + mctx.getString(R.string.ver_str)+";"+ terminal_info+";"+myscr+";id:"+myub);
					mHttpGet.setHeader("Accept-Encoding", "gzip");
					mHttpGet.setHeader("Cookie", cookie);

					mHttpResponse = mHttpClient.execute(mHttpGet);

					solvingChallenge = false;
				}
			}
			
			// Redirect used... 
			Header[] azz = mHttpResponse.getHeaders("Location");
			if(azz.length > 0){
				String newurl = azz[0].getValue();
				mHttpGet = null;
				mHttpGet = new HttpGet(newurl);
				mHttpGet.setHeader("User-Agent", "aguamarina-" + mctx.getString(R.string.ver_str)+";"+ terminal_info+";"+myscr+";id:"+myub);
				mHttpGet.setHeader("Accept-Encoding", "gzip");
				
				mHttpResponse = null;
				mHttpResponse = mHttpClient.execute(mHttpGet);
				
				
			}
			return mHttpResponse;
		}catch(Exception e){
			System.out.println("=============================================");
			e.printStackTrace();
			System.out.println("=============================================");
			return null;
		}
		
		//catch(IOException e) {return null; }
		
		
	}
	
	
	public static HttpResponse getHttpResponse(String url, Context mctx){
		try{
			//DbHandler db = new DbHandler(mctx);

			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 12000);
			HttpConnectionParams.setSoTimeout(httpParameters, 12000);

			DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
			mHttpClient.setRedirectHandler(new RedirectHandler() {

				public boolean isRedirectRequested(HttpResponse response,
						HttpContext context) {
					return false;
				}

				public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
					return null;
				}
			});
			
			HttpGet mHttpGet = new HttpGet(url);
			//mHttpGet.setHeader("User-Agent", "aguamarina-" + mctx.getString(R.string.ver_str)+";fetch_icon");

			HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
			
			
			// Redirect used... 
			Header[] azz = mHttpResponse.getHeaders("Location");
			if(azz.length > 0){
				String newurl = azz[0].getValue();
				mHttpGet = null;
				mHttpGet = new HttpGet(newurl);
				
				mHttpResponse = null;
				mHttpResponse = mHttpClient.execute(mHttpGet);
				
				
			}
			return mHttpResponse;
		}catch(Exception e) {return null; }
		
	}
	
	public static DefaultHttpClient createItOpen(String url){
		try{
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 12000);
		HttpConnectionParams.setSoTimeout(httpParameters, 12000);

		DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
		mHttpClient.setRedirectHandler(new RedirectHandler() {

			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				return false;
			}

			public URI getLocationURI(HttpResponse response, HttpContext context)
			throws ProtocolException {
				return null;
			}
		});
		
		mHttpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				// TODO Auto-generated method stub
				return 0;
			}
		});
		
		mHttpClient.setReuseStrategy(new ConnectionReuseStrategy() {
			
			public boolean keepAlive(HttpResponse response, HttpContext context) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		return mHttpClient;
		}catch (Exception e) {return null;	}
		
	}
	
	public static HttpResponse fetch(String fetch_file, DefaultHttpClient mHttpClient){
		try{
		HttpGet mHttpGet = new HttpGet(fetch_file);
		
		HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
		
		// Redirect used... 
		Header[] azz = mHttpResponse.getHeaders("Location");
		if(azz.length > 0){
			String newurl = azz[0].getValue();
			mHttpGet = null;
			mHttpGet = new HttpGet(newurl);
			
			mHttpResponse = null;
			mHttpResponse = mHttpClient.execute(mHttpGet);
		}
		
		return mHttpResponse;
		}catch (Exception e) {return null;	}
		
	}
	
	
	
	public static HttpResponse imgWsGet(String url){
		try{
						
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 12000);
			HttpConnectionParams.setSoTimeout(httpParameters, 12000);
			
			DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
			mHttpClient.setRedirectHandler(new RedirectHandler() {

				public boolean isRedirectRequested(HttpResponse response,
						HttpContext context) {
					return false;
				}
				

				public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
					return null;
				}
			});
			
			
			HttpGet mHttpGet = new HttpGet(url);

			
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
			
			return mHttpResponse;
		}catch(Exception e){
			System.out.println("=============================================");
			e.printStackTrace();
			System.out.println("=============================================");
			return null;
		}
	
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
