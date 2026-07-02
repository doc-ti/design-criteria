
package edu.doc_ti.phivalidation.gendata.bitstreamstats ;

import java.io.BufferedOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BitstreamElement {

	public static String DELIMITER = ";" ;

	private String MAC;
	private String SerialNumber ;
	private String Firmware ;
	private String IPV4 ;
	private String IPV6 ;
	private String Telefono;
	
	private long PaquetesTx = 0 ;
	private long PaquetesRx = 0 ;
	private long ErroresTx = 0 ;
	private long ErroresRx = 0 ;

	private long tsBase = System.currentTimeMillis() ;
	private long uptime = 0 ;
	SimpleDateFormat sdfNice = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	boolean reset ;
	
//	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//	static SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMMdd_HHmmss");
//	static Date tsTo = null ;
//	static String path = "." ;
//	static int maxFiles = 20 ;

	private String SitesArr[] = {
			 "www.google.com",
			 "microsoft.com",
			 "google.com",
			 "amazon.com",
			 "twitter.com",
			 "facebook.com",
			 "cisco.com",
			 "get.net.playstation.net",
			 "whatsapp.com",
			 "x.com",
			 "github.com",
			 "youtube.com",
			 "instagram.com",
			 "www.microsoft.com"
	};

	class PingInfo {
		public PingInfo(int pos) {
			site = SitesArr[pos%SitesArr.length] ;
			if ( myFaker.getElements().getProbabilityLowerThan(0.2) ) {
				pingV4 = "-1" ;
				pingV6 = String.format(Locale.US, "%.3f", myFaker.getElements().exponentialDistributedNumberAsDouble(1) ) ;
				PacketLossV6 = "0%" ;
				if ( myFaker.getElements().getProbabilityLowerThan(0.01) ) {
					PacketLossV6 = myFaker.getElements().nextInt(1, 10) + "%" ;
				}
			} else {
				pingV6 = "-1" ;
				pingV4 = String.format(Locale.US, "%.3f", myFaker.getElements().exponentialDistributedNumberAsDouble(1) ) ;
				PacketLossV4 = "0%" ;
				if ( myFaker.getElements().getProbabilityLowerThan(0.02) ) {
					PacketLossV4 = myFaker.getElements().nextInt(1, 10) + "%" ;
				}
			}
		}

		String site = "" ;
		String pingV4 = "" ;
		String PacketLossV4 = null ;
		String pingV6 = "" ;
		String PacketLossV6 = null ;
		
		@SuppressWarnings("unchecked")
		public Object getJSON() {
			JSONObject jsonObject = new JSONObject();
			
			jsonObject.put("site", site) ;
			jsonObject.put("pingV4", pingV4) ;
			jsonObject.put("pingV6", pingV6) ;
			if ( PacketLossV4 != null) {
				jsonObject.put("PacketLossV4", PacketLossV4) ;
			}
			if ( PacketLossV6 != null) {
				jsonObject.put("PacketLossV6", PacketLossV6) ;
			}
			
			return jsonObject;
		}
	}
	
	ArrayList<PingInfo> arrPings = new ArrayList<PingInfo>() ; 
	
	
	public String getMAC() {
		return MAC;
	}

	public String getSerialNumber() {
		return SerialNumber;
	}


	public String getFirmware() {
		return Firmware;
	}


	public String getIPV4() {
		return IPV4;
	}


	public String getIPV6() {
		return IPV6;
	}


	public String getTelefono() {
		return Telefono;
	}

	public String getUptime() {
		return Long.toString(uptime) ;
	}

	public Long getUptimeLong() {
		return (uptime) ;
	}

	public static CustomFakerBitstreamStats getMyFaker() {
		return myFaker;
	}

	static CustomFakerBitstreamStats myFaker = new CustomFakerBitstreamStats();
	
	public static void main(String[] args) {
		
		BitstreamElement sobgen = new BitstreamElement() ;
		
		for (int  n=0 ;n<5 ; n++) {
			sobgen.generateData() ;
			sobgen.printDataAsJSON(); 
		}
	}


	public BitstreamElement() {
		MAC = generateMAC() ;
		SerialNumber = generateSerialNumber() ;
		Firmware = generateFirmware() ;
		IPV4 = generateIPV4() ;
		IPV6 = generateIPV6() ;
		Telefono = generateTelefono() ;
	}
	

	private String generateTelefono() {
		return myFaker.regexify("9[1-8]") + myFaker.number().digits(8) ;
	}


	private String generateIPV4() {
		return myFaker.internet().publicIpV4Address();
	}

	private String generateIPV6() {
		return myFaker.internet().ipV6Cidr();
	}

	private String generateFirmware() {
		//	"Firmware": "03.03.01.031L",
		return myFaker.regexify("03\\.0[1-2]\\.0[1-3]\\.03[1-2]L" ) ;
	}

	private String generateMAC() {
		return myFaker.regexify("[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}:[A-F0-9]{2}") ;
	}
	
	private String generateSerialNumber() {
		return myFaker.regexify("ARLT[0-9]{3}[A-D][0-9]{3}") ;
	}

	public void generateData() {
		generateData(900) ;
	}

	public void generateData( int interval ) {
		reset = false ;
		if ( myFaker.getElements().getProbabilityLowerThan(0.005) ) {
			resetData() ;
		}
		
//		String aux = myFaker.date().between( new Date(tsBase +secondsFromStart*1000 ), new Date(tsBase +secondsFromStart*1000 ),  "yyyy-MM-dd'T'HH:mm:ss" ) ;
		uptime += interval + myFaker.getElements().nextInt(-5, 5) ;

		PaquetesRx += myFaker.getElements().exponentialDistributedNumberAsLong(100000.0) ;
		PaquetesTx += myFaker.getElements().exponentialDistributedNumberAsLong(10000.0) ;
		
		if ( myFaker.getElements().getProbabilityLowerThan(0.02) ) {
			ErroresRx += myFaker.getElements().exponentialDistributedNumberAsLong(100.0) ;
		}
		if ( myFaker.getElements().getProbabilityLowerThan(0.02) ) {
			ErroresTx += myFaker.getElements().exponentialDistributedNumberAsLong(100.0) ;
		}
		


		int startPos = myFaker.getElements().nextInt(0, SitesArr.length) ;
		int max= myFaker.getElements().nextInt(3, 7) ;

		arrPings.clear();
		for ( int pos = 0 ; pos<max ; pos++) {
			arrPings.add( new PingInfo(pos+startPos) ) ;
		}
		
//		System.out.println (MAC + " ; " + MAC) ;
//		System.out.println (SerialNumber) ;
//		System.out.println (Firmware) ;
//		System.out.println (IPV4 + " - " + IPV6 + " - " + Telefono ) ;

//		Schema<String, ?> schema =
//	        Schema.of(
//	        		Field.field("MAC", () -> getMAC() )
//	        		, Field.field("uptime", () -> getUptime() )
//	        		, Field.field("SerialNumber", () -> getSerialNumber() )
//	        		, Field.field("Firmware", () -> getFirmware() )
//	        		, Field.field("IPV4", () -> getIPV4() )
//	        		, Field.field("IPV6", () -> getIPV6() )
////	        		, addressesField
//			);
		
//        Field<String, String> nameField = Field.field("name", () -> myFaker.name().fullName());
//        Field<String, String> emailField = Field.field("email", () -> myFaker.internet().emailAddress());


//        Field<String, List<Object>> addressesField = Field.field("addresses", () -> {
//            // Crear lista de direcciones
//            List<Object> addresses = new ArrayList<>();
//            IntStream.range(0, faker.number().numberBetween(1, 4)) // Generar de 1 a 3 direcciones
//                    .forEach(i -> {
//                        // Crear un objeto dirección
//                        addresses.add(new Address(
//                                faker.address().city(),
//                                faker.address().zipCode()
//                        ));
//                    });
//            return addresses;
//        });     
//        
//        Schema<String, String> schema2 = Schema.of(
//                nameField,
//                emailField,
//                addressesField
//        );		
		
		
//		JsonTransformer<String> transformer = JsonTransformer.<String>builder().build();
//		String json = transformer.generate(schema, 1);
//
//		System.out.println(json) ;
//		 
//		CsvTransformer<String> transformerCSV =
//			        CsvTransformer.<String>builder().header(false).separator(DELIMITER).build();
//
//		String csv = transformerCSV.generate(schema, 1);
//		System.out.println(csv) ;
//		 
//		XmlTransformer<String> transformerXML =
//			        new XmlTransformer.XmlTransformerBuilder<String>().build();
//
//		String xml = transformerXML.generate(schema, 1).toString();
//		System.out.println(xml) ;
			 		 

//		 Schema<String, Object> schema =
//			        Schema.of(
//			        		Field.field("@timestamp", () -> myFaker.date().past(30, TimeUnit.DAYS, "yyyy-MM-dd'T'HH:mm:ss"))
//			        		, Field.field("first_name", () -> myFaker.name().firstName())
//			        		, Field.field("last_name", () -> myFaker.name().lastName())
//			        		, Field.field("address", () -> myFaker.address().streetAddress())
//			        		, Field.field("country", () -> myFaker.address().countryCode())
//			        		, Field.field("god", () -> myFaker.ancient().god())
//			        		, Field.field("animal", () -> myFaker.animal().name())
//			        		, Field.field("author", () -> myFaker.book().author())
//			        		, Field.field("genre", () -> myFaker.book().genre())
//			        		, Field.field("title", () -> myFaker.book().title())
//			        		, Field.field("phone", () -> myFaker.phoneNumber().phoneNumberNational())
//			        		, Field.field("superhero", () -> myFaker.superhero().name())
//			        		, Field.field("url", () -> myFaker.internet().url())
//			        		
//			        		);
//
//		 JsonTransformer<String> transformer = new JsonTransformer<>();
//		 String json = transformer.generate(schema, 2);
//		 
//		 System.out.println(json) ;

	}
	
	private void printDataAsJSON() {
		System.out.println( getAsJSONString(true, true) ) ;
	}

	private void resetData() {
		PaquetesTx = 0 ;
		PaquetesRx = 0 ;
		ErroresTx = 0 ;
		ErroresRx = 0 ;
		tsBase = System.currentTimeMillis() ;
		uptime = 0 ;
		reset = true ;
	}

	@SuppressWarnings("unused")
	private void printData() {
		System.out.println (MAC ) ;
		System.out.println ( sdfNice.format(new Date( tsBase + uptime*1000 ) ) ) ;
		System.out.println (SerialNumber) ;
		System.out.println (Firmware) ;
		System.out.println (uptime) ;
		System.out.println (IPV4 + " - " + IPV6 + " - " + Telefono ) ;

		for ( PingInfo pinfo : arrPings ) {
			System.out.println ( "\t" + pinfo.site 
					+ " / "  + pinfo.pingV4 
					+ " / " + pinfo.PacketLossV4 
					+ " / " + pinfo.pingV6 
					+ " / " + pinfo.PacketLossV6 ) ;
		}
	}

	
    private static String prettyPrintJson(String jsonString) {
        StringBuilder prettyJson = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;

        for (char charFromJson : jsonString.toCharArray()) {
            switch (charFromJson) {
                case '"':
                    prettyJson.append(charFromJson);
                    inQuotes = !inQuotes; // Alternar el estado de las comillas
                    break;
                case '{':
                case '[':
                    prettyJson.append(charFromJson);
                    if (!inQuotes) {
                        prettyJson.append("\n").append("  ".repeat(++indent));
                    }
                    break;
                case '}':
                case ']':
                    if (!inQuotes) {
                        prettyJson.append("\n").append("  ".repeat(--indent));
                    }
                    prettyJson.append(charFromJson);
                    break;
                case ',':
                    prettyJson.append(charFromJson);
                    if (!inQuotes) {
                        prettyJson.append("\n").append("  ".repeat(indent));
                    }
                    break;
                case ':':
                    prettyJson.append(charFromJson);
                    if (!inQuotes) {
                        prettyJson.append(" ");
                    }
                    break;
                default:
                    prettyJson.append(charFromJson);
            }
        }
        return prettyJson.toString();
    }

	
	@SuppressWarnings("unchecked")
	public String getAsJSONString(boolean nice, boolean addReset) {
		
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("MAC", MAC) ;
		jsonObject.put("@timestamp", sdfNice.format(new Date( tsBase + uptime*1000 ) ) );
		jsonObject.put("uptime", uptime) ;
		jsonObject.put("SerialNumber", SerialNumber) ;
		jsonObject.put("Firmware", Firmware) ;
		jsonObject.put("IPV4", IPV4) ;
		jsonObject.put("IPV6", IPV6) ;
		jsonObject.put("Telefono", Telefono) ;
		jsonObject.put("PaquetesRx", PaquetesRx) ;
		jsonObject.put("PaquetesTx", PaquetesTx) ;
		jsonObject.put("ErroresRx", ErroresRx) ;
		jsonObject.put("ErroresTx", ErroresTx) ;
		
		JSONArray pingsArr= new JSONArray();
		
		jsonObject.put("pings", pingsArr) ;
        
		for ( PingInfo pinfo : arrPings ) {
			pingsArr.add(pinfo.getJSON() ) ;
		}

		if ( addReset ) {
			jsonObject.put("reset", reset) ;
		}
		
        // Imprimir el JSON en formato de texto
        String jsonString = jsonObject.toJSONString();
        if ( ! nice )
        	return jsonString ;

        return (prettyPrintJson(jsonString));
		
	}
	
	public String getDat() {
		return "" ;
	}

	public String getAsPlain() {
		StringBuilder buffer = new StringBuilder(); 

		buffer.append(MAC) ;
		buffer.append("|") ;
		buffer.append(sdfNice.format(new Date( tsBase + uptime*1000 ) ) );
		buffer.append("|") ;
		buffer.append(Telefono) ;		
		buffer.append("|") ;
		buffer.append(IPV4) ;

		
		for ( PingInfo pinfo : arrPings ) {
			buffer.append("|") ;
			buffer.append(pinfo.pingV4) ;
		}

		return buffer.toString();
	}

	
	/*
	{
		"MAC": "D4:86:60:EE:E8:0D",
		"fecha": "2024-09-03 22:33:48",
		"SerialNumber": "ARLT209C1688",
		"Firmware": "03.03.01.031L",
		"IPv4": "90.69.177.104",
		"IPv6": "",
		"Telefono": "828040148",
		"StatusTelefono": "1",
		"PaquetesTx": "647949",
		"PaquetesRx": "410136",
		"ErroresTx": "0",
		"ErroresRx": "0",
		"pings": [
			{
				"SITE": "google.com",
				"pingv4": "37.360",
				"PacketLossv4": "0%",
				"pingv6": "-1"
			}
	]
	 */
	
}
