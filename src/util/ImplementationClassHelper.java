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
    public Obj currentDesignator;
    public Obj currentDesignatorAssignop;
    public Obj firstDesignator;
    public Struct currentType;
    public Struct currentExprType;
    public boolean hasReturn = false;
    public boolean inAssign = false;
    public static boolean errorFlag = false;
    public boolean factorComesFromDesignator = false;
    public static int globalVarCount = 0;
    public static int globalArrayCount = 0;
    public static int localVarCount = 0;
    public static int localArrayCount = 0;
    public static int constVarCount = 0;
    public static int globalFuncCount = 0;
    public static int staticFuncCount = 0;
    public static int mulop = 0;
    public static int addop = 0;
    public static int blockCount = 0;
    public static int funcCallCount = 0;
    public static int opoffset = 100;
    
    public static final Struct boolType = new Struct(Struct.Bool);
    public String designatorName = "";
    
    public Logger log = Logger.getLogger(getClass());
    
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
        Tab.insert(Obj.Type, "bool", boolType);
        Tab.openScope();
        //report_info("Program zapocet");
        return prog;
    }
    
    public void finalizeProgram(Obj prog) {
        Code.dataSize = Tab.currentScope().getnVars();
        Tab.chainLocalSymbols(prog);
        Tab.closeScope();
        //report_info("Kraj programa");
    }
    
    public void startMethod() {
        if (currentMethod != null) {
           currentMethod.setAdr(Code.pc);
            if("main".equals(currentMethod.getName())){
                    Code.mainPc = currentMethod.getAdr();
            }
            Code.put(Code.enter);
            Code.put(currentMethod.getLevel()); // broj argumenata
            Code.put(Tab.currentScope().getnVars()); // broj lok. promenljivih 
        }
    }
    
    public void finalizeMethod() {
        if (currentMethod != null) {
            if(hasReturn == false && currentMethod.getType() != Tab.noType) {
                report_error("Greska: funkcija " + currentMethod.getName() + " nema pravilan return iskaz");
            } else {
                
                Code.put(Code.exit);
                Code.put(Code.return_);
                Tab.chainLocalSymbols(currentMethod);
                Tab.closeScope();
                currentMethod = null;
                hasReturn = false;
            }  
        }
    }
    
    public void insertMethod(Struct retType, String name, int line) {
            if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Metoda "+ name + " je vec deklarisana na liniji",line);
        } else {
            if(retType == null) {
                currentMethod = Tab.insert(Obj.Meth, name, Tab.noType);
                currentType = Tab.noType;
            }
            else {
                currentMethod = Tab.insert(Obj.Meth, name, retType);
                currentType = retType;
            }
            Tab.openScope();
            report_info("Obradjuje se metoda " + name + " na liniji",line);
        }
    }
    
    public void insertConstVar(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Con, name, currentType);
            constVarCount++;
            report_info("Deklasirana globalna konstanta " + name + " na liniji",line);
        }
    }
    
    public void insertConstVar(String name,Object val, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else if ( (val instanceof Integer && currentType == Tab.intType) || 
                 (val instanceof Character && currentType == Tab.charType) || 
                 (val instanceof Boolean && currentType == boolType)) {
                
                Obj obj = Tab.insert(Obj.Con, name, currentType);
                int adr = 0;
                if (val instanceof Integer) {
                    adr = (Integer)val;
                } else if (val instanceof Character) {
                    adr = (int) ((Character) val);
                } else if (val instanceof Boolean) {
                    adr = (Boolean) val ? 1 : 0;
                }

                obj.setAdr(adr);
                constVarCount++;
                report_info("Deklasirana globalna konstanta " + name + " na liniji",line);
        } else {
           report_error("Greska: promenjljiva " + name + " nije kompatibilna sa tipom " + val.toString() + ". Greska na liniji",line );
       }
    }
    
    public void insertGlobalVar(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, currentType);
            globalVarCount++;
            report_info("Deklasirana globalna promenljiva " + name + " na liniji",line);
        }
    }
    
    public void insertGlobalArray(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, new Struct (Struct.Array, currentType));
            globalArrayCount++;
            report_info("Deklasiran globalni niz " + name + " na liniji",line);
        }
    }
    
    
    public void insertLocalVar(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, currentType);
            localVarCount++;
            report_info("Deklasirana lokalna promenljiva " + name + " na liniji",line);
        }
    }
    
    public void insertLocalArray(String name, int line) {
        if(Tab.currentScope().findSymbol(name) != null) {
            report_error("Greska: promenljiva " + name + " je vec deklarisana. Greska na liniji",line );
        } else {
            Obj obj = Tab.insert(Obj.Var, name, new Struct (Struct.Array, currentType));
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
    
    public Struct insertBool(Boolean bool, int line) { 
        Obj obj = Tab.insert(Obj.Con,"", boolType);
        obj.setAdr( (bool == true) ? 1 : 0);
        Code.load(obj);  // Stavljanje na stek constante
        return boolType;
    }
    
    public Struct insertArray(Struct type, Struct exp, int line) {
        isIntType(exp, line);
        
        Obj obj = new Obj(Obj.Elem,"",new Struct(Struct.Array,type));
        Code.put(Code.newarray);
        if (type == Tab.charType) Code.put(0);
        else Code.put(1);
        
        return type;
    }
    
    public Struct insertExpr(Struct exp, int line) {
        
        return exp;
    }
    
    public Struct findType(String tName, int line) {
        Obj obj = Tab.find(tName);
        if( obj == Tab.noObj || Obj.Type != obj.getKind()) {
            report_error("Greska: Nije pronadjen tip: " + tName + " na liniji",line);
        } 
        return obj.getType();
    }
    
    public Obj resolveIdent(String tName, Obj des, int line) {
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
        if (des != null) {
            return des;
        }
        return obj;
    }
    
    public Obj resolveIdent(String tName, int line) {
        Obj obj = Tab.find(tName);
        if( obj == Tab.noObj) {
            report_error("Greska: " + tName + " nije deklarisano. na liniji",line);
        } else {
            currentDesignator = obj;
        }
        return obj;  
    }
    
    public Struct resolveAddopResult(Struct res) {
        if (res != null) {
            return res;
        }
        return currentExprType;
    }
    
    public void resolveDesignator(Integer op) {
        if (op > 100) { // If operation is PLUS_EQUAL or DIV_EQUAL etc, have to push designator to stack
            if (!isArray(firstDesignator.getType())) {
                Code.load(firstDesignator);
            } else {
                Code.put(Code.dup2);
                Code.load(firstDesignator);
            }
        }
        
    }
    
    public Obj resolveDesignatorArray (int line) {
        if (currentDesignator.getType().getKind() != Struct.Array) {
            report_error("Greska: promenljiva nije niz, na liniji",line);
        }
        Code.load(currentDesignator);
        Obj obj = new Obj (Obj.Elem, currentDesignator.getName(), new Struct(Struct.Array, currentDesignator.getType().getElemType())); 
        currentDesignator = obj;
        return obj;
    }
    
    public Struct resolveType(Struct type,int pos) {
        currentExprType = type;   
        factorComesFromDesignator = false;
        if (pos == 0) Code.put(Code.neg);
        return type;
    }
    
    public Struct loadAgain(Struct tName,Integer op) {
        if (op > 100) { // If operation is PLUS_EQUAL or DIV_EQUAL etc, have to push designator to stack
            
            Obj obj = Tab.find(designatorName);
            if( obj == Tab.noObj) {
                report_error("Greska: leva strana addop-a (" + designatorName + ") nije deklarisano. na liniji");
            }
            currentDesignatorAssignop = obj;
            
            
            if (isArray(currentDesignatorAssignop.getType())) {
                Code.put(Code.dup2);
                Code.put(Code.dup2);
                if (currentDesignatorAssignop.getType().getKind() == Struct.Char) Code.put(Code.baload);
                else Code.put(Code.aload);
            }
                
            
        } else if (isArray(tName) && !inAssign) {
            Code.load(currentDesignator);
        } else if (currentDesignatorAssignop != null && isArray(tName)) {
            if (isArray(currentDesignator.getType()) && !currentDesignatorAssignop.equals(currentDesignator)) {
                Code.load(currentDesignator);
            }
        }
        
        return tName;
    }
    
    public Struct pushDesignatorToStack(Obj des) {
        if (!isArray(des.getType())) {
            Code.load(des);
        }
        currentDesignator = des;
        designatorName = des.getName();
        factorComesFromDesignator = true;
        return des.getType();
    }
    
    public void setFirstDesignator(Obj des) {
        currentDesignator = des;
        firstDesignator = des;
    }
    
    public Struct callFunc(int line) {
        if( Obj.Meth != currentDesignator.getKind()) {
            report_error("Greska: " + currentDesignator.getName() + " nije funkcija, na liniji",line);
            return Tab.noType;
        } else {
            
            int destAdr = currentDesignator.getAdr() - Code.pc;
            Code.put(Code.call);
            Code.put2(destAdr);
            if (currentDesignator.getType() != Tab.noType) {
                Code.put(Code.pop);
            }
            funcCallCount++;
            report_info("Pronadjen poziv funkcije " + currentDesignator.getName() + " na liniji",line);
            return currentDesignator.getType();
        }
    }
    
    public void returnMatched(Struct type, int line) {
        if (currentMethod != null) {
            if( type != null) {
                hasReturn = true;
                if(!type.compatibleWith(currentMethod.getType())) {
                    report_error("Greska: nekompatibilni return izrazi, na liniji",line);
                }
            } else if (currentMethod.getType() != Tab.noType){
                hasReturn = false;
                report_error("Greska: nekompatibilni return izrazi, na liniji",line);
            }    
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
    
    public Obj isCompatible(Obj obj, int line) {
        if(obj.getType().equals(currentExprType)) {
            return obj;
        }
        report_error("Greska: nekompatibilni tipovi, na liniji",line);
        return Tab.noObj;
    }
    
    public Struct isCompatible(Struct type,Obj obj, int line) {
        if(type.assignableTo(obj.getType())) {
            return type;
        } else if (isArray(obj.getType())) {
            if (isArray(type)) {
                if(obj.getType().getElemType().assignableTo(type.getElemType())) return type;
            } else {
                if(obj.getType().getElemType().assignableTo(type)) return type;
            }
        } else if (isArray(type)) {
            if(obj.getType().assignableTo(type.getElemType())) return type;
        }
        report_error("Greska: nekompatibilni tipovi, na liniji",line);
        return Tab.noType;
    }
    
    public Struct isCompatible(Struct t1,Struct t2, int line) {
        if(t1.compatibleWith(t2)) {
            return t1;
        } else if (isArray(t2)) {
            if (isArray(t1)) {
                if(t2.getElemType().assignableTo(t1.getElemType())) return t1;
            } else {
                if(t2.getElemType().assignableTo(t1)) return t1;
            }
        } else if (isArray(t1)) {
            if(t2.assignableTo(t1.getElemType())) return t1;
        }
        report_error("Greska: nekompatibilni tipovi, na liniji",line);
        return Tab.noType;
    }
    
    public Obj isDesignatorVar(Obj des, int line) {
        if(des.getKind() != Obj.Var && des.getKind() != Obj.Elem && des.getKind() != Obj.Con) {
            report_error("Greska: designator nije promenljiva, na linij",line);
        } else {
            currentDesignator = des;
        }
        return des;
    }
    
    public void isIntType(int line) {
        // is current designator an int type. Used for INC and DEC
        if (!currentDesignator.getType().equals(Tab.intType)) {
            report_error("Greska: promenljiva "+currentDesignator.getName()+" mora biti tipa int, na liniji",line);
        }
    }
    
    public Struct isIntType(Struct type, int line) {
        Struct typeToCheck = (type.getKind()==Struct.Array)?type.getElemType():type;
        // is Term an int type, used for MINUS TERM
        if (!typeToCheck.equals(Tab.intType)) {
            report_error("Greska: podatak mora biti tipa int, na liniji",line);
            return Tab.noType;
        }
        return Tab.intType;
    }
    
    public void isBoolType(Struct type, int line) {
        // is current designator an int type. Used for INC and DEC
        if (!type.equals(boolType)) {
            report_error("Greska: tip mora biti bool, na liniji",line);
        }
    }
    
    public Boolean isArray(Struct st) {
        return (st.getKind() == Struct.Array) ? true : false;
    }
    
    public Struct executeIncrement (int line) {
        isIntType(line);
        
        Code.load(currentDesignator);
        Code.loadConst(1);
        Code.put(Code.add);
        Code.store(currentDesignator);
        
        return Tab.intType;
    }
    
    public Struct executeDecrement (int line) {
        isIntType(line);
        
        Code.load(currentDesignator);
        Code.loadConst(1);
        Code.put(Code.sub);
        Code.store(currentDesignator);
        
        return Tab.intType;
    }
    
    public Struct executeAddop(Struct t1, Integer op, Struct t2, int line) {
        isCompatible(t1, t2, line);
        
        if (op > 100) {
            Code.put(op - 100);
//            Code.store(currentDesignatorAssignop);
//            Code.load(currentDesignatorAssignop);
            if (isArray(currentDesignatorAssignop.getType())) {
                
                 if (currentDesignatorAssignop.getType().getKind() == Struct.Char) {
                    Code.put(Code.bastore);
                    Code.put(Code.baload);
                 }
                 else {
                    Code.put(Code.astore);
                    Code.put(Code.aload);
                 } 
                
            } else {
                Code.store(currentDesignatorAssignop);
                Code.load(currentDesignatorAssignop);
            }

        } else {
            Code.put(op);
        }
        
        return Tab.intType;
    }

    public Struct executeMulop(Struct t1, Integer op, Struct t2, int line) {
        isCompatible(t1, t2, line);
        
        if (op > 100) {
            Code.put(op - 100);
            if (isArray(currentDesignatorAssignop.getType())) {
                
                 if (currentDesignatorAssignop.getType().getKind() == Struct.Char) {
                    Code.put(Code.bastore);
                    Code.put(Code.baload);
                 }
                 else {
                    Code.put(Code.astore);
                    Code.put(Code.aload);
                 } 
                
            } else {
                Code.store(currentDesignatorAssignop);
                Code.load(currentDesignatorAssignop);
            }
        } else {
            Code.put(op);
        }
        
        return Tab.intType;
    }
    
    public Struct executeAssignop(Struct var,Integer op, int line) {
        isDesignatorVar(currentDesignator, line);
        if(!isArray(var)) isCompatible(var,currentDesignator,line);
        
        if (op == 100) { //EQUAL
            Code.store(firstDesignator);
        } else if (op > 100) { //PLUS_EQUAL etc.
            Code.put(op - 100);
            Code.store(firstDesignator);
        } else { // OTHER MAYBE NOTHING
//            Code.put(op);
        }
        inAssign = false;
        return var;
    }
    
    public Struct executeBoolOperation(Struct eName1,Integer oper,Struct eName2,int line) {
        if (!eName1.compatibleWith(eName2)) {
            report_error("Greska: nekompatibilni tipovi, na liniji",line);
        } else {
            switch (oper) {
                //EQUAL
                case(0): 
                    break;
                //NOT EQUAL
                case(1): 
                    break;
                //LESSER
                case(2): 
                    break;
                //LESSER EQUAL
                case(3): 
                    break;
                //GREATER
                case(4): 
                    break;
                //GREATER EQUAL
                case(5): 
                    break;
            }
        }
        return boolType;
    }
    
    public void read(Obj dName, int line) {
        if(dName.getType() != Tab.intType && dName.getType() != Tab.charType && dName.getType() != boolType) {
            report_error("Greska: operand instrukcije READ mora biti char, int ili bool. Na liniji",line);
        } else {
            Code.put(Code.read);
            Code.store(dName);
        }        
    }
    
    public void print(Struct eName,int length, int line) {
        Struct typeToCheck = (eName.getKind()==Struct.Array)?eName.getElemType():eName;
        if(typeToCheck != Tab.intType && typeToCheck != Tab.charType && typeToCheck != boolType ) {
            report_error("Greska: operand instrukcije PRINT mora biti char, int ili bool. Na liniji",line);
        } else {
            if (typeToCheck == Tab.intType) {
                Code.loadConst(length);
                Code.put(Code.print);
            } else if (typeToCheck == Tab.charType) {
                Code.loadConst(length);
                Code.put(Code.bprint);
            }   
        }
    }
    
    public void print(Struct eName, int line) {
        Struct typeToCheck = (eName.getKind()==Struct.Array)?eName.getElemType():eName;
        if(typeToCheck != Tab.intType && typeToCheck != Tab.charType && typeToCheck != boolType) {
            report_error("Greska: operand instrukcije PRINT mora biti char, int ili bool. Na liniji",line);
        } else {
            if (typeToCheck == Tab.intType) {
                Code.loadConst(5);
                Code.put(Code.print);
            } else if (typeToCheck == Tab.charType) {
                Code.loadConst(1);
                Code.put(Code.bprint);
            }
        }
    }
}
