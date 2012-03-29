package utils;

import java.io.RandomAccessFile;

public class FileWriter implements Runnable{

	private int index;
	private int offset;
	private byte[] data;
	private RandomAccessFile file = null;

	public FileWriter(int pieceIndex,int offset, byte[] data){
		this.index = pieceIndex;
		this.offset = offset;
		this.data = data;
	}

	public void run(){
		Manager.fileLock.lock();
		try{
			file = Manager.getFile();
			file.write(data,index*Manager.torrent_info.piece_length+offset,data.length);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Manager.fileLock.unlock();
		}
	}

}
