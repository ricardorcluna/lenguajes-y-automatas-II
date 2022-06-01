/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: ___________    HORA: ___________ HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un metodo por cada simbolo
 *:                 No-Terminal de la gramatica mas el metodo emparejar ().
 *:                 El analisis empieza invocando al metodo del simbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 22/Feb/2015 FGil                -Se mejoro errorEmparejar () para mostrar el
 *:                                 numero de linea en el codigo fuente donde 
 *:                                 ocurrio el error.
 *: 08/Sep/2015 FGil                -Se dejo lista para iniciar un nuevo analizador
 *:                                 sintactico.
 *:----------------------------------------------------------------------------
 */
package compilador;

import javax.swing.JOptionPane;

public class SintacticoSemantico {

    private Compilador cmp;
    private boolean    analizarSemantica = false;
    private String     preAnalisis;
    
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //

    public SintacticoSemantico(Compilador c) {
        cmp = c;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metodo que inicia la ejecucion del analisis sintactico predictivo.
    // analizarSemantica : true = realiza el analisis semantico a la par del sintactico
    //                     false= realiza solo el analisis sintactico sin comprobacion semantica

    public void analizar(boolean analizarSemantica) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *7
        programa();
    }

    //--------------------------------------------------------------------------

    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;            
        } else {
            errorEmparejar( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }
    
    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
 
    private void errorEmparejar(String _token, String _lexema, int numLinea ) {
        String msjError = "";

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasigna")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + ( _lexema.equals ( "$" )? "fin de archivo" : _lexema ) + 
                    ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico

    private void error(String _descripError) {
        cmp.me.error ( cmp.ERR_SINTACTICO, 
                       _descripError + 
                       "Linea: " + cmp.be.preAnalisis.numLinea );
    }

    // Fin de error
    //--------------------------------------------------------------------------
    // * * *   AQUI EMPIEZA  EL CODIGO DE LOS PROCEDURES    * * *
    //--------------------------------------------------------------------------

    private void programa () {   
            declaraciones ();
            declaraciones_subprogramas ();
            proposiciones_optativas ();
            emparejar( "end" );
    }
    
    private void declaraciones () {
        if ( preAnalisis.equals( "dim" )){
            emparejar ( "dim" );
            lista_declaraciones ();
            declaraciones ();
        } else {
            //declaraciones -> empty
        }
    }
    
    private void lista_declaraciones () {
        if ( preAnalisis.equals( "id" )){
            emparejar ( "id" );
            emparejar ( "as" );
            tipo ();
            lista_declaracione();
        } else {
             error ( "[lista_declaraciones]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void lista_declaracione () {
        if ( preAnalisis.equals( "," )){
            emparejar ( "," );
            lista_declaraciones ();
        } else {
            //lista_declaracione -> empty
        }
    }
    
    private void tipo () {
        if ( preAnalisis.equals( "integer" )){
            emparejar ( "integer" );
        } else if( preAnalisis.equals( "single" )){
            emparejar ( "single" );
        } else if( preAnalisis.equals( "string" )){
            emparejar ( "string" );
        } else {
            error ( "[tipo]: Se espera recibir un  integer | single | string" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void declaraciones_subprogramas () {
        if ( preAnalisis.equals( "function" ) || preAnalisis.equals( "sub" )){
            declaracion_subprograma ();
            declaraciones_subprogramas ();
        } else {
            //declaraciones_subprogramas -> empty
        }
    }
    
    private void declaracion_subprograma () {
        if ( preAnalisis.equals( "function" )){
            declaracion_funcion ();
        } else if(preAnalisis.equals( "sub" )){
            declaracion_subrutina ();
        } else {   
            error ( "[declaracion_subprograma]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void declaracion_funcion () {
        if ( preAnalisis.equals( "function" )){
            emparejar ( "function" );
            emparejar ( "id" );
            argumentos ();
            emparejar ( "as" );
            tipo ();
            proposiciones_optativas ();
            emparejar ( "end" );
            emparejar ( "function" );
        } else {
           error ( "[declaracion_funcion] se esperaba la declaracion de una funcion" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void declaracion_subrutina () {
        if ( preAnalisis.equals( "sub" )){
            emparejar ( "sub" );
            emparejar ( "id" );
            argumentos ();
            proposiciones_optativas ();
            emparejar ( "end" );
            emparejar ( "sub" );
        } else {
           error ( "[declaracion_subrutina]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void argumentos () {
        if ( preAnalisis.equals( "(" )){
            emparejar ( "(" );
            lista_declaraciones ();
            emparejar ( ")" );
        } else {
            //declaraciones_subprogramas -> empty
        }
    }
    
    private void proposiciones_optativas () {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "call" ) || preAnalisis.equals( "if" ) || preAnalisis.equals( "do" )){
            proposicion ();
            proposiciones_optativas ();
        } else {
            //proposiciones optativas -> empty
        }
    }
    
        private void proposicion () {
        if ( preAnalisis.equals( "id" )){
            emparejar ( "id" );
            emparejar ( "opasig" );
            expresion ();
        } else if ( preAnalisis.equals( "call" )){
            emparejar ( "call" );
            emparejar ( "id" );
            proposicio ();
        } else if ( preAnalisis.equals( "if" )){
            emparejar ( "if" );
            condicion ();
            emparejar ( "then" );
            proposiciones_optativas ();
            emparejar ( "else" );
            proposiciones_optativas ();
            emparejar ( "end" );
            emparejar ( "if" );
        } else if ( preAnalisis.equals( "do" )){
            emparejar ( "do" );
            emparejar ( "while" );
            condicion ();
            proposiciones_optativas ();
            emparejar ( "loop" );
        } else {
            error ( "[proposicion]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
        
    private void proposicio () {
        if ( preAnalisis.equals( "(" )){
            emparejar ( "(" );
            lista_expresiones ();
            emparejar ( ")" );
        } else {
            //declaraciones_subprogramas -> empty
        }
    }
        
    private void lista_expresiones () {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) || preAnalisis.equals( "(" )){
            expresion ();
            lista_expresione ();
        } else {
            //declaraciones_subprogramas -> empty
        }
    }
    
    private void lista_expresione () {
        if ( preAnalisis.equals( "," )){
            emparejar ( "," );
            expresion ();
            lista_expresione ();
        } else {
            //lista_declaracione -> empty
        }
    }
    
    private void condicion () {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) || preAnalisis.equals( "(" )){
            expresion ();
            emparejar ( "oprel" );
            expresion ();
        } else {
            error ( "[condicion]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void expresion () {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) || preAnalisis.equals( "(" )){
            termino ();
            expresio ();
        } else if (preAnalisis.equals( "literal" )){
            emparejar ( "literal" );
        } else {
            error ( "[condicion]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void expresio () {
        if ( preAnalisis.equals( "opsuma" )){
            emparejar ( "opsuma" );
            termino ();
            expresio ();
        } else {
            //expresio -> empty
        }
    }
    
    private void termino () {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) || preAnalisis.equals( "(" )){
            factor ();
            termin ();
        } else {
           error ( "[termino]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void termin () {
        if ( preAnalisis.equals( "opmult" )){
            emparejar ( "opmult" );
            factor ();
            termin ();
        } else {
            //expresio -> empty
        }
    }
    
    private void factor () {
        if ( preAnalisis.equals( "id" )){
            emparejar ( "id" );
            facto ();
        } else if ( preAnalisis.equals( "num" )){
           emparejar ( "num" );
        }  else if ( preAnalisis.equals( "num.num" )){
           emparejar ( "num.num" );
        } else if ( preAnalisis.equals( "(" )){
            emparejar ( "(" );
            expresion ();
            emparejar ( ")" );
        } else {
           error ( "[factor]" +
                    "No. de linea: " + cmp.be.preAnalisis.numLinea
                  );
        }
    }
    
    private void facto () {
        if ( preAnalisis.equals( "(" )){
            emparejar ( "(" );
            lista_expresiones ();
            emparejar ( ")" );
        } else {
            //facto -> empty
        }
    }
    
}

//------------------------------------------------------------------------------
//::