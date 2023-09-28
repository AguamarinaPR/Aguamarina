	package cm.aptoide.pt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

public class ExtrasRssHandler extends DefaultHandler{
	
	Context mctx = null;
	String server = null;
	
	private DbHandler db = null;
	
	private boolean extras = false;
	private boolean pkg = false;
	private boolean apkid = false;
	private boolean cmt = false;
	
	private String e_apkid = null;
	private String e_cmt = null;
	
	public ExtrasRssHandler(Context ctx, String srv){
		mctx = ctx;
		server = srv;
		db = new DbHandler(mctx);
		e_apkid = "";
		e_cmt = "";
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		if(apkid){
			e_apkid = e_apkid.concat(new String(ch).substring(start, start + length));
		}else if(cmt){
			e_cmt = e_cmt.concat(new String(ch).substring(start, start + length));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		if(localName.trim().equals("extras")){
			extras = false;
			// end of XML document
		}else if(localName.trim().equals("pkg")){
			pkg = false;
			// Add fetched information to DB
			db.addExtraXML(e_apkid, e_cmt, server);
			e_apkid = "";
			e_cmt = "";
		}else if(localName.trim().equals("apkid")){
			apkid = false;
		}else if(localName.trim().equals("cmt")){
			cmt = false;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if(localName.trim().equals("extras")){
			extras = true;
		}else if(localName.trim().equals("pkg")){
			pkg = true;
		}else if(localName.trim().equals("apkid")){
			apkid = true;
		}else if(localName.trim().equals("cmt")){
			cmt = true;
		}	
	}

}
