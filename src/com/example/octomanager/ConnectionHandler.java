package com.example.octomanager;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

//public class MyListener implements ActionListener{
//	
//}
public class ConnectionHandler extends AsyncTask<Void, Void, Void>{
	// 91.194.61.201
	public static String serverip = "91.194.61.201";
	public static int serverport = 12345;
	Socket s;
//	public DataInputStream dis;
	public InputStream dis;
//	public DataOutputStream dos;
	public OutputStream dos;
	public String message;
	protected ArrayList<SocketListenerInterface> listenersList = new ArrayList<SocketListenerInterface>();
	protected ArrayList<byte[]> buffer = new ArrayList<byte[]>();

	
	@Override
	protected Void doInBackground(Void... params) {
		
		if ( null == s){
			Log.wtf("AsyncTank","doInBackground: empty socket");
		}else if (s.isBound()){
			Log.w("AsyncTank","doInBackground: bound socket");
		}else if (s.isClosed()){
			Log.w("AsyncTank","doInBackground: closed socket");
		}else if (s.isConnected()){
			Log.w("AsyncTank","doInBackground: connected socket");
		}
		
		if( null != s && s.isConnected()){
			Log.w("AsyncTank", "doInBackground: already connected");
			return null;
		}else{
			Log.w("AsyncTank", "doInBackground: connecting");
		}
	    try {
	        Log.i("AsyncTank", "doInBackground: Creating Socket");
	        s = new Socket(serverip, serverport);
	    } catch (Exception e) {
	        Log.e("AsyncTank", "doInBackground: Cannot create Socket",e);
	        return null;
	    }
	    if (s.isConnected()) {
	        try {
	            Log.i("AsyncTank", "doInBackground: do InputStream");
	        	dis = s.getInputStream();
	            Log.i("AsyncTank", "doInBackground: doOutputStream");
	            dos = s.getOutputStream();
	            Log.i("AsyncTank", "Send on ready");
	            Log.i("AsyncTank", "doInBackground: Socket created, Streams assigned");
	
	        } catch (Exception e) {
	            Log.i("AsyncTank", "doInBackground: Cannot assign Streams, Socket not connected");
	            e.printStackTrace();
	    	    return null;
	        }
	    } else {
	        Log.i("AsyncTank", "doInBackground: Cannot assign Streams, Socket is closed");
		    return null;
	    }
        onReady();
	    return null;
	}
	
	public void addListener (SocketListenerInterface listener){
		
		listenersList.add(listener);
		
	}	
	
	private void onReady() {
		
		// Empty buffer 
		sendBuffer();
		
		// signal availability to listeners
		 for( Iterator<SocketListenerInterface> itr = listenersList.iterator(); itr.hasNext(); ){
			 
			 SocketListenerInterface theListener = (SocketListenerInterface) itr.next();
			 theListener.onSocketReady(); 
			 
		 }
		
	}
	
	public boolean send( byte[] bytes){
		
		buffer.add(bytes);
		if( sendBuffer()){
			return true;
		}
		return false;
		
	}

	private boolean sendBuffer(){
		if( buffer.isEmpty()){
			return true;
		}
		for( Iterator<byte[]> i = buffer.iterator(); i.hasNext(); ){
			byte[] theBytes =  i.next();
			writeToStream( theBytes );
			
		}
		return false;
	}

	public byte[] readFromStream() {
//		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//		int nRead;
//		byte[] data = new byte[16384];
//	    try {
//	        if (null != s && s.isConnected()) {
//	            Log.i("AsynkTask", "readFromStream : Reading message");
//	    		while ((nRead = dis.read(data, 0, data.length)) != -1) {
//	    			buffer.write(data, 0, nRead);
//    			}
//	    		buffer.flush();
//	        } else {
//	            Log.i("AsynkTask", "readFromStream : Cannot Read, Socket is closed");
//	        }
//	    } catch (Exception e) {
//	        Log.i("AsynkTask", "readFromStream : Reading failed ", e);
//	    }
//		return buffer.toByteArray();
		byte[] ret = null;
		ret[0] = 0x12;
		ret[1] = 0x13;
		return ret;
	}

	public boolean writeToStream(byte[] theBytes) {

	    try {
	        if (s.isConnected()){
	            Log.i("AsynkTask", "writeToStream : Writing message");
	            dos.write(theBytes);
	            return true;
	            
	        } else {
	            Log.i("AsynkTask", "writeToStream : Cannot write to stream, Socket is closed");
	            return false;
	        }
	    } catch (Exception e) {
	        Log.i("AsynkTask", "writeToStream : Writing failed ",e);
	    }	
	    return false;
    }


	public void writeToStream(String string) {
		Log.i("AsynkTask", "writeToStream string : "+string);
		byte[] bytes = string.getBytes();
		writeToStream(bytes);
	}
	
	@Override
  protected void onCancelled() {

		try {
			s.close();
		} catch (IOException e) {
			Log.w("AsynkTask::onCancelled", "Failed to socket");
			e.printStackTrace();
			return;
		}
		Log.i("AsynkTask::onCancelled", "Closed socket successfully");
	}
		

}