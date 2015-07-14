package com.thricethree.musiclibrary;

import android.util.Log;

import com.thricethree.musiclibrary.model.AlbumInfo;
import com.thricethree.musiclibrary.model.ArtistInfo;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by thricethree on 7/14/15.
 */
public class ArtistInfoSAX extends DefaultHandler {

    private static final String LOGTAG = "ArtistInfoSAX";
    private static final boolean DEBUG = true;

    private final ArtistInfo artistInfo = new ArtistInfo();
    private final StringBuilder buffer = new StringBuilder();
    //	private int albumRank;
//	private String smallUrl;
//	private String medUrl;
    AlbumInfo albumInfo;
    private ArrayList<AlbumInfo> artistAlbums = new ArrayList<AlbumInfo>();

    public ArtistInfoSAX() {
        Log.d(LOGTAG, "ArtistInfoSAX called");
    }

    public ArtistInfo parse (byte[] bytes) throws Exception {
        final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        parse(is);
        is.close();
//		artistInfo.addAlbumToList(albumInfo);
        return artistInfo;
    }

    public ArtistInfo parse (InputStream is) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(is, this);
//		artistInfo.addAlbumToList(albumInfo);
        return artistInfo;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    public void startDocument () throws SAXException {
        super.startDocument();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    public void endDocument () throws SAXException {
        super.endDocument();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement (String uri, String localName, String name, Attributes attributes)
            throws SAXException
    {
        super.startElement (uri, localName, name, attributes);
        if (name.equals("topalbums")) {
            artistInfo.setArtistName(attributes.getValue("artist"));
        }
        if (name.equals("album")){
            albumInfo = new AlbumInfo();
        }
//		if (name.equals("image") && albumInfo != null ){
//			if (attributes.getValue("size").equals("small")){
//				albumInfo.setAlbumCoverUrlSmall(localName.getBytes().toString());
//			}
//			if (attributes.getValue("size").equals("medium")){
//				albumInfo.setAlbumCoverUrlMedium(attributes.getValue("size"));
//			}
//		}
//		if (name.equals("album") && Integer.parseInt(attributes.getValue("rank")) != albumRank){
//			albumRank = Integer.parseInt(attributes.getValue("rank"));
//		}
//		if (name.equals("image") && attributes.getValue("size").equals("small")){
//			smallUrl = localName.getBytes().toString();
//		}
    }




    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters (char[] ch, int start, int length)
            throws SAXException
    {
        super.characters(ch, start, length);
        buffer.append(ch, start, length);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement (String uri, String localName, String name)
            throws SAXException
    {
//        if (DEBUG) Log.d (LOGTAG, "endElement="+localName);
        super.endElement(uri, localName, name);
        parseEndElement (uri, localName, name, buffer.toString());
        buffer.delete(0, buffer.length());
    }


    //  Encapsulate actual parsing specifics here.
    private void parseEndElement (String uri, String localName, String name, String value)
    {
        if (DEBUG) {
            Log.d (LOGTAG, "localName= "+localName);
            Log.d (LOGTAG, "value= "+value);
            Log.d (LOGTAG, "name= "+name);
        }
        value = value.trim();  //  may not be appropriate for all parsing situations
//        albumInfo = new AlbumInfo();
        if (localName.equals("artist") && albumInfo != null) {
//        	Log.d (LOGTAG, "name="+name);
//        	Log.d (LOGTAG, "value="+value);
            albumInfo.setArtistName(value);
            return;
        }
        if (localName.equals("name") && albumInfo != null && !value.equals(artistInfo.getArtistName())) {
//        	Log.d (LOGTAG, "name="+name);
//        	Log.d (LOGTAG, "value="+value);
            albumInfo.setAlbumTitle(value);
            return;
        }
        if (localName.equals("mbid")) {
            artistInfo.setArtistID(value);
            return;
        }
        if (localName.equals("image") && albumInfo.getAlbumCoverUrlSmall() == null) {
            albumInfo.setAlbumCoverUrlSmall(value);
            return;
        }
        if (localName.equals("album")) {
            artistAlbums.add(albumInfo);
            albumInfo = null;
            return;
        }
        if (localName.equals("topalbums")) {
            artistInfo.setArtistAlbums(artistAlbums);
            return;
        }

    }

}
