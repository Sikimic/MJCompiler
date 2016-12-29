/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import org.apache.log4j.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

/**
 *
 * @author Sikimic Nebojsa
 */
public class ImplementationClassHelper {
    
    public Obj currentMethod;
    public Struct currentType;
    public boolean hasReturn = false;
    public Logger log = Logger.getLogger(getClass());
    public static int globalVarCount = 0;
    public static int globalArrayCount = 0;
    public static int localVarCount = 0;
    public static int localArrayCount = 0;
    public static int constVarCount = 0;
    public static int globalFuncCount = 0;
    public static int staticFuncCount = 0;
    public static int blockCount = 0;
    public static int funcCallCount = 0;
    
    
    public static boolean errorFlag = false;
    
    public void report_error(String msg, int line) {
        errorFlag = true;
        //System.err.println(msg + " " + line);
        log.error(msg.toString() + " " + line);
    }
    
    public void report_error(String msg) {
        errorFlag = true;
        //System.err.println(msg);
        log.error(msg.toString());
    }
    
    public void report_info(String msg, int line) {
        //System.out.println(msg + " " + line);
        log.info(msg.toString() + " " + line);
    }
    
    public void report_info(String msg) {
        //System.out.println(msg);
        log.info(msg.toString());
    }
        
    public Obj startProgram(String name) {
        Obj prog = Tab.insert(Obj.Prog, name, Tab.noType);
        Tab.openScope();
        //report_info("Program zapocet");
        return prog;
    }
    
    public void finalizeProgram(Obj prog) {
        Tab.chainLocalSymbols(prog);
        Tab.closeScope();
        //report_info("Kraj programa");
    }
    
    public void startMethod() {
        currentMethod.setAdr(Code.pc);
        
        if("main".equals(currentMethod.getName())){
                Code.mainPc = currentMethod.getAdr();
        }
        Code.put(Code.enter);
        Code.put(currentMethod.getLevel()); // broj argumenata
        Code.put(Tab.currentScope().getnVars()); // broj lok. promenljivih
    }
    
    public void finalizeMethod() {
        if(hasReturn == false && currentType != Tab.noType) {
            report_error("Greska: funkcija " + currentMethod.getName() + " nema pravilan return iskaz");
        } else {
            Tab.chainLocalSymbols(currentMethod);
            Tab.closeScope();
            currentMethod = null;
            hasReturn = false;
        }
    }
    
    public void insertMethod(Struct retType, String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Metoda "+ name + " je vec deklarisana na liniji",line);
        } else {
            if(retType == null) {
                currentMethod = Tab.insert(Obj.Meth, name, Tab.noType);
                currentType = null;
            }
            else {
                currentMethod = Tab.insert(Obj.Meth, name, retType);
                currentType = retType;
            }
        }
        Tab.openScope();
        report_info("Obradjuje se metoda " + name + " na liniji",line);
    }
    
    public void insertConstVar(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenjljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Con, name, currentType);
            constVarCount++;
            report_info("Deklasirana globalna konstanta " + name + " na liniji",line);
        }
    }
    
    public void insertGlobalVar(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenjljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, currentType);
            globalVarCount++;
            report_info("Deklasirana globalna promenljiva " + name + " na liniji",line);
        }
    }
    
    public void insertGlobalArray(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenjljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, currentType);
            globalArrayCount++;
            report_info("Deklasiran globalni niz " + name + " na liniji",line);
        }
    }
    
    
    public void insertLocalVar(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenjljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, currentType);
            localVarCount++;
            report_info("Deklasirana lokalna promenljiva " + name + " na liniji",line);
        }
    }
    
    public void insertLocalArray(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenjljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, currentType);
            localArrayCount++;
            report_info("Deklasiran lokalni niz " + name + " na liniji",line);
        }
    }
    
    public Struct insertNumber(Integer con, int line) { 
        Obj obj = Tab.insert(Obj.Con,"", Tab.intType);
        obj.setAdr(con.intValue());
        Code.load(obj);  // Stavljanje na stek constante
        return Tab.intType;
    }
    
    public Struct insertChar(Character con, int line) { 
        Obj obj = Tab.insert(Obj.Con,"", Tab.charType);
        obj.setAdr(con.charValue());
        Code.load(obj);  // Stavljanje na stek constante
        return Tab.charType;
    }
    
    public Struct findType(String tName, int line) {
        Obj obj = Tab.find(tName);
        if( obj == Tab.noObj || Obj.Type != obj.getKind()) {
            report_error("Greska: Nije pronadjen tip: " + tName + " na liniji",line);
        } 
        return obj.getType();
    }
    
    public Obj resolveIdent(String tName, int line) {
        Obj obj = Tab.find(tName);
        if( obj == Tab.noObj) {
            report_error("Greska: " + tName + " nije deklarisano. na liniji",line);
        } else {
            if (obj.getKind() == Obj.Con) 						
                    report_info("Detektovano koriscenje konstante " + tName + " na liniji", line);
            else {
                if (obj.getKind() == Obj.Var) {
                    if (obj.getLevel() == 0) 
                        report_info("Detektovano koriscenje globalne promenljive " + tName + " na liniji", line);
                    else 
                        report_info("Detektovano koriscenje lokalne promenljive " + tName + " na liniji", line);
                }
            }
        }
        return obj;
    }
    
    public Struct callFunc(Obj obj, int line) {
        if( Obj.Meth != obj.getKind()) {
            report_error("Greska: " + obj.getName() + " nije funkcija, na liniji",line);
            return Tab.noType;
        } else {
            funcCallCount++;
            report_info("Pronadjen poziv funkcije " + obj.getName() + " na liniji",line);
            return obj.getType();
        }
    }
    
    public void returnMatched(Struct type, int line) {
        
        if( type != null) {
            hasReturn = true;
            if(!type.compatibleWith(currentMethod.getType())) {
                report_error("Greska: nekompatibilni return izrazi, na liniji",line);
            }
        } else if (currentMethod.getType() != Tab.noType){
            report_error("Greska: nekompatibilni return izrazi, na liniji",line);
        }
    }
    
    public void returnAssignable(Obj t1, Struct t2, int line) {
        hasReturn = true;
        if(!t1.getType().assignableTo(t2)) {
            report_error("Greska: izraz " + t2 + " se ne moze dodeliti izrazu " + t1 + ", u liniji",line);
        }
    }
    
    public Struct equalTypes(Struct t1, Struct t2, int line) {
        if(t1.equals(t2)) {
            return t1; 
        }
        report_error("Greska: nekompatibilni tipovi, na liniji",line);
        return Tab.noType;
    }
    
    public Struct equalTypes(Struct t1, Struct t2, Struct typeOperation, int line) {
        if(t1.equals(t2) && t1.equals(typeOperation)) {
            return t1;
        }
        report_error("Greska: nekompatibilni tipovi, na liniji",line);
        return Tab.noType;
    }
    
    public void print(Struct eName, int line) {
        if(eName != Tab.intType && eName != Tab.charType) {
            report_error("Greska: operand instrukcije PRINT mora biti char ili int, na liniji",line);
        }
        if (eName == Tab.intType) {
            Code.loadConst(5);
            Code.put(Code.print);
        } else if (eName == Tab.charType) {
            Code.loadConst(1);
            Code.put(Code.bprint);
        }
        
    }
}
