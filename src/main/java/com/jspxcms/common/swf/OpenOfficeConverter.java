package com.jspxcms.common.swf;

import java.io.File;
import java.net.ConnectException;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

// D:\programcommon\openoffice4\program\soffice -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard
// /opt/openoffice4/program/soffice -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard &
// ./soffice "-accept=socket,host=localhost,port=8100;urp;StarOffice.ServiceManager" -nologo -headless -nofirststartwizard
public class OpenOfficeConverter {
	// private static final Logger logger = LoggerFactory
	// .getLogger(OpenOfficeConverter.class);

	public static void main(String[] args) throws Exception {
		File src1 = new File("d:/Jspxcms.docx");
		File dest1 = new File("d:/Jspxcms.pdf");
		String host = "127.0.0.1";
		int port = 8100;
		doc2pdf(src1, dest1, host, port);
	}

	public static void doc2pdf(File from, File to, String host, int port)
			throws ConnectException {
		OpenOfficeConnection conn = new SocketOpenOfficeConnection(host, port);
		try {
			conn.connect();
			DocumentConverter converter = new OpenOfficeDocumentConverter(conn);
			converter.convert(from, to);
		} finally {
			if (conn.isConnected()) {
				conn.disconnect();
			}
		}
	}
}
