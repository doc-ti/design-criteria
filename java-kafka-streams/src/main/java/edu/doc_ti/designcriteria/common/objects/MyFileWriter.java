package edu.doc_ti.designcriteria.common.objects;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class MyFileWriter {
	
	static int sequence = 0 ;
	
	private static synchronized int getCount() {
		return sequence++ ;
	}
	

	private static Logger LOG = LoggerFactory.getLogger(MyFileWriter.class);
	SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	
	BufferedOutputStream bout = null ;
	int numRecordsAux = 0 ;
	int numRecordsTotal = 0 ;
	int maxRecords = 5000 ;
	
	String filesPathTmp = "" ;
	String filesPathClosed= "" ;
	String basename = "" ;
	String actualFilename = "" ;
	private boolean hasHeader = true ;
	
    public static void createDirectory(String pathIn) {
        
        File dir= new File(pathIn);

        if (!dir.exists()) {
            boolean creado = dir.mkdirs(); // crea todos los directorios necesarios
            if (creado) {
            	LOG.info("Path {} created ", pathIn);
            } else {
                LOG.error("Path cannot be created {}", pathIn);
                System.exit(-1);
            }
        } 
    }
	
	
	public MyFileWriter(String filesPathIn, String basenameIn, int maxRecordsIn, boolean hasHeaderIn) {
		hasHeader  = hasHeaderIn ;
		filesPathTmp = filesPathIn + "/tmp-files/";
		filesPathClosed = filesPathIn + "/closed-files/" ;
		
		createDirectory(filesPathTmp);
		createDirectory(filesPathClosed);
		
		maxRecords = maxRecordsIn ;
		basename = basenameIn ;
		if ( maxRecords < 1000 ) 
			maxRecords = 1000 ;
	}

	private void openFile(ObjStreaming obj) {
		try {
			actualFilename = basename + "-" + sdf.format(new Date()) + "-" + String.format("%07d", getCount() );
			bout = new BufferedOutputStream(new FileOutputStream(filesPathTmp + actualFilename  + ".tmp")) ;
			if ( hasHeader ) {
				bout.write( obj.getHeader().getBytes() );
				bout.write("\n".getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void closeFile() {
		try {
			LOG.info("Closed file {} with {} records", actualFilename, numRecordsAux);
			bout.close() ;
			bout = null ;
			Files.move(new File( filesPathTmp + actualFilename + ".tmp"), new File( filesPathClosed + actualFilename  ));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeToFile( ObjStreaming obj) {
		
		if ( numRecordsAux >= maxRecords  ) {
			closeFile() ;
			numRecordsAux = 0 ;
		}
		
		if ( bout == null) {
			openFile(obj) ;
		}
		
		numRecordsAux++ ;
		numRecordsTotal++ ;
		
		try {
			bout.write( obj.toDataString().getBytes() );
			bout.write("\n".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeJsonToFile( ObjStreaming obj) {
		
		if ( numRecordsAux >= maxRecords  ) {
			closeFile() ;
			numRecordsAux = 0 ;
		}
		
		if ( bout == null) {
			openFile(obj) ;
		}
		
		numRecordsAux++ ;
		numRecordsTotal++ ;
		
		try {
			bout.write( obj.toJSonString().getBytes() );
			bout.write("\n".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
