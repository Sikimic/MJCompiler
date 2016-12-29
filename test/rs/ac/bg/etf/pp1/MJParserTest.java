package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java_cup.runtime.Symbol;


import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import util.ImplementationClassHelper;

import util.Log4JUtils;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
                    File sourceCode = new File("test/mjcompiler/program.mj");
                    log.info("Compiling source file: " + sourceCode.getAbsolutePath());

                    br = new BufferedReader(new FileReader(sourceCode));
                    Yylex lexer = new Yylex(br);

                    MJParser p = new MJParser(lexer);
                    Symbol s = p.parse();  //pocetak parsiranja

                    log.info("Print calls = " + p.printCallCount);
                    Tab.dump();
                    
                    if(ImplementationClassHelper.errorFlag == false) {
                        File output = new File("test/program.obj");
                        if (output.exists()) output.delete();
                        Code.write(new FileOutputStream(output));
                        log.info("Parsiranje uspesno zavrseno!");
                    } else {
                        log.error("Greske pri parsiranju!");
                    }
                    
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
