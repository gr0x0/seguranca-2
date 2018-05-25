import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DigestCalculator {
	
	public static void main(String[] args) {    
		String[] args2 = {"SHA1","resources\\Arquivo.txt"};
		if (args2.length < 2) {            
			System.err.println("Use: java DigestCalculator Tipo_Digest Caminho_Arq1 Caminho_Arq2 ... Caminho_ArqN Caminho_ArqListaDigest");            
			System.exit(1);        
		}
				
		if (args2[0].equals("SHA1") || args2[0].equals("MD5")){
		}else {
			System.err.println("Tipo de Digest inválido. Insira SHA1 ou MD5.");            
			System.exit(1);
		}
		List<String> argList = Arrays.asList(args2);
		List<String> files = argList.subList(1, argList.size()-1);        
		String DigestType = args2[0];
		String digestsFilePath = args2[args2.length-1];
		File file = new File(digestsFilePath);
		if(file.exists() == false) {            
			System.out.println("Caminho_ArqListaDigest nao existe.");
			System.out.println("Criando um arquivo com o nome de: " + digestsFilePath + "\n");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		int i = 0;
		for(String filePath : files) {            
			i++;            
			file = new File(filePath);            
			if(file.exists() == false) {                
				System.err.println(filePath+" (Caminho_Arq"+i+") nao e um caminho valido.");                
				System.exit(1);            
			}        
		}               
		// check all files        
		for(String filePath : files) {            
			try {                
				byte[] fileBytes = getFileBytes(new File(filePath));                
				byte[] digest;                
				String digestHex;                
				String fileName;
				String line;              
				MessageDigest messageDigest = MessageDigest.getInstance(args2[0]);                    
				messageDigest.update(fileBytes);
				digest = messageDigest.digest();
				digestHex = toHex(digest);
				fileName = getFileName(filePath);
				// digest check and output
				line = contains(fileName, digestsFilePath);
				if(line != null){
					if(line.contains(DigestType)){
						if(line.contains(digestHex)){
							System.out.println(fileName +" "+ DigestType +" "+ digestHex +" (OK)");
						}else{
							System.out.println(fileName +" "+ DigestType +" "+ digestHex +" (NOT OK)");
						}
					}else{
						System.out.println(fileName +" "+ DigestType +" "+ digestHex +" (NOT FOUND)");
						appendDigestToFile(digestsFilePath, fileName, DigestType, digestHex, line);
						//Chamada da função tem que ser feita aqui!!!!
					}
				}else{
					System.out.println(fileName +" "+ DigestType +" "+ digestHex +" (NOT FOUND)");
					System.out.println("Adicionando no arquivo.");
					appendDigestToFileBottom(digestsFilePath, fileName, DigestType, digestHex);
				}
			} catch (IOException e) {                
				e.printStackTrace();            
			} catch (NoSuchAlgorithmException e) {                
				e.printStackTrace();            
			} catch (Exception e) {                
				e.printStackTrace();            
			}        
		}    
	}
	
	
	private static void appendDigestToFileBottom(String digestsFilePath, String fileName, String digestType, String digestHex) {        
		try {            
			FileWriter fw = new FileWriter(digestsFilePath, true);            
			BufferedWriter out = new BufferedWriter(fw);            
			out.write(fileName +" "+ digestType +" "+ digestHex);
			out.newLine();
			out.close();        
		} catch(IOException e) {            
			e.printStackTrace();
        }    
	}
	
	private static void appendDigestToFile(String digestsFilePath, String fileName, String digestType, String digestHex, String line) {        
		String strfim = line + "[" + digestType + " " + digestHex + "]";
		File fil = new File(digestsFilePath);  
	    try{
		    FileReader fr = new FileReader(fil);
		    BufferedReader br = new BufferedReader(fr);

		    String linha = br.readLine();
		    ArrayList<String> salvar = new ArrayList();
		    
		    while(linha != null){
		        
		        if(linha.contains(fileName) == false){
		            salvar.add(linha);
		        }else{
		        	salvar.add(strfim);
		        }
		        
		        linha = br.readLine();
		    }
		    br.close();
		    fr.close();
		    FileWriter fw2 = new FileWriter(fil, true);
		    fw2.close();
		    
		    
		    FileWriter fw = new FileWriter(fil);
		    BufferedWriter bw = new BufferedWriter(fw);
		    
		    for(int i = 0; i<salvar.size(); i++){
		        bw.write( salvar.get(i) );
		        bw.newLine();
		    }
		    bw.close();
		    fw.close();
	    
	    }catch(IOException ex){
	        
	    }   
	}
	
	
	private static byte[] getFileBytes(File file) throws IOException {        
		// check file        
		if (file.exists() == false) {            
			return null;        
		}        
		// verify file size        
		long length = file.length();        
		int maxLength = Integer.MAX_VALUE;        
		if (length > maxLength){
			throw new IOException(String.format("The file %s is too large", file.getName()));
		}
		int len = (int) length;        
		byte[] bytes = new byte[len];        
		InputStream in = new FileInputStream(file);        
		// read bytes        
		int offset = 0, n = 0;        
		while (offset < len && n >= 0) {            
			n = in.read(bytes, offset, len - offset);            
			offset += n;        
		}        
		if (offset < len){
			throw new IOException("Failed to read the full content from: " + file.getName());
		}
		in.close();
		return bytes;
		}    
	
	private static String getFileName(String filePath) {
		String[] fileParts = filePath.split("\\\\");
		String fileName = fileParts[fileParts.length-1];
		return fileName;
	} 
	
	private static String toHex(byte[] byteSequence) {        
		StringBuffer buf = new StringBuffer();        
		for (int i = 0; i < byteSequence.length; i++) {            
			String hex = Integer.toHexString(0x0100 + (byteSequence[i] & 0x00FF)).substring(1);            
			buf.append((hex.length() < 2 ? "0" : "") + hex);        
		}		
		return buf.toString();    
	}    
	
	private static String contains(String str, String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String read = null;
		try {
			while( br.ready() ){
				read = br.readLine();
				if(read.contains(str)){
					return read;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		br.close();
		return null;
	}
}
