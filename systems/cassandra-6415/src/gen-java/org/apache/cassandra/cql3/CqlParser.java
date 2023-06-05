// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g 2023-06-04 19:35:38

    package org.apache.cassandra.cql3;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Collections;
    import java.util.EnumSet;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;

    import org.apache.cassandra.auth.Permission;
    import org.apache.cassandra.auth.DataResource;
    import org.apache.cassandra.auth.IResource;
    import org.apache.cassandra.cql3.*;
    import org.apache.cassandra.cql3.statements.*;
    import org.apache.cassandra.cql3.functions.FunctionCall;
    import org.apache.cassandra.db.marshal.CollectionType;
    import org.apache.cassandra.exceptions.ConfigurationException;
    import org.apache.cassandra.exceptions.InvalidRequestException;
    import org.apache.cassandra.exceptions.SyntaxException;
    import org.apache.cassandra.utils.Pair;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CqlParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "K_USE", "K_SELECT", "K_DISTINCT", "K_COUNT", "K_AS", "K_FROM", "K_WHERE", "K_ORDER", "K_BY", "K_LIMIT", "K_ALLOW", "K_FILTERING", "K_WRITETIME", "K_TTL", "INTEGER", "K_AND", "K_ASC", "K_DESC", "K_INSERT", "K_INTO", "K_VALUES", "K_IF", "K_NOT", "K_EXISTS", "K_USING", "K_TIMESTAMP", "K_UPDATE", "K_SET", "K_DELETE", "K_BEGIN", "K_UNLOGGED", "K_COUNTER", "K_BATCH", "K_APPLY", "K_CREATE", "K_KEYSPACE", "K_WITH", "K_COLUMNFAMILY", "K_PRIMARY", "K_KEY", "K_COMPACT", "K_STORAGE", "K_CLUSTERING", "K_CUSTOM", "K_INDEX", "IDENT", "K_ON", "STRING_LITERAL", "K_TRIGGER", "K_DROP", "K_ALTER", "K_TYPE", "K_ADD", "K_RENAME", "K_TO", "K_TRUNCATE", "K_GRANT", "K_REVOKE", "K_LIST", "K_OF", "K_NORECURSIVE", "K_MODIFY", "K_AUTHORIZE", "K_ALL", "K_PERMISSIONS", "K_PERMISSION", "K_KEYSPACES", "K_USER", "K_SUPERUSER", "K_NOSUPERUSER", "K_USERS", "K_PASSWORD", "QUOTED_NAME", "FLOAT", "BOOLEAN", "UUID", "HEXNUMBER", "K_NAN", "K_INFINITY", "K_NULL", "QMARK", "K_TOKEN", "K_IN", "K_ASCII", "K_BIGINT", "K_BLOB", "K_BOOLEAN", "K_DECIMAL", "K_DOUBLE", "K_FLOAT", "K_INET", "K_INT", "K_TEXT", "K_UUID", "K_VARCHAR", "K_VARINT", "K_TIMEUUID", "K_MAP", "S", "E", "L", "C", "T", "F", "R", "O", "M", "A", "W", "H", "N", "D", "K", "Y", "I", "U", "P", "G", "B", "X", "V", "Z", "J", "Q", "DIGIT", "LETTER", "HEX", "EXPONENT", "WS", "COMMENT", "MULTILINE_COMMENT", "';'", "'('", "')'", "','", "'\\*'", "'['", "']'", "'.'", "'-'", "'{'", "':'", "'}'", "'='", "'+'", "'<'", "'<='", "'>'", "'>='"
    };
    public static final int T__144=144;
    public static final int T__143=143;
    public static final int K_COMPACT=44;
    public static final int T__146=146;
    public static final int T__145=145;
    public static final int K_SET=31;
    public static final int T__140=140;
    public static final int HEXNUMBER=80;
    public static final int K_MAP=101;
    public static final int T__142=142;
    public static final int K_KEY=43;
    public static final int K_INDEX=48;
    public static final int T__141=141;
    public static final int QMARK=84;
    public static final int K_KEYSPACES=70;
    public static final int K_ASC=20;
    public static final int K_CREATE=38;
    public static final int K_APPLY=37;
    public static final int K_USING=28;
    public static final int K_PASSWORD=75;
    public static final int MULTILINE_COMMENT=134;
    public static final int T__137=137;
    public static final int T__136=136;
    public static final int T__139=139;
    public static final int T__138=138;
    public static final int T__135=135;
    public static final int K_SUPERUSER=72;
    public static final int K_UNLOGGED=34;
    public static final int QUOTED_NAME=76;
    public static final int K_ORDER=11;
    public static final int K_DROP=53;
    public static final int K_CLUSTERING=46;
    public static final int K_USE=4;
    public static final int K_VARINT=99;
    public static final int K_NORECURSIVE=64;
    public static final int A=111;
    public static final int B=122;
    public static final int C=105;
    public static final int K_BLOB=89;
    public static final int D=115;
    public static final int K_ALTER=54;
    public static final int K_PERMISSIONS=68;
    public static final int E=103;
    public static final int K_LIMIT=13;
    public static final int K_INSERT=22;
    public static final int F=107;
    public static final int G=121;
    public static final int H=113;
    public static final int K_FROM=9;
    public static final int I=118;
    public static final int K_PRIMARY=42;
    public static final int K_NAN=81;
    public static final int J=126;
    public static final int K_LIST=62;
    public static final int K=116;
    public static final int L=104;
    public static final int K_BIGINT=88;
    public static final int M=110;
    public static final int N=114;
    public static final int COMMENT=133;
    public static final int O=109;
    public static final int K_INET=94;
    public static final int P=120;
    public static final int Q=127;
    public static final int R=108;
    public static final int K_DESC=21;
    public static final int S=102;
    public static final int T=106;
    public static final int U=119;
    public static final int V=124;
    public static final int K_BOOLEAN=90;
    public static final int W=112;
    public static final int X=123;
    public static final int Y=117;
    public static final int Z=125;
    public static final int K_USER=71;
    public static final int T__151=151;
    public static final int K_WITH=40;
    public static final int T__150=150;
    public static final int T__152=152;
    public static final int K_OF=63;
    public static final int K_MODIFY=65;
    public static final int K_DECIMAL=91;
    public static final int K_VARCHAR=98;
    public static final int K_STORAGE=45;
    public static final int K_ON=50;
    public static final int K_UUID=97;
    public static final int K_ALLOW=14;
    public static final int K_CUSTOM=47;
    public static final int K_VALUES=24;
    public static final int K_BATCH=36;
    public static final int K_ASCII=87;
    public static final int EXPONENT=131;
    public static final int T__148=148;
    public static final int T__147=147;
    public static final int K_INFINITY=82;
    public static final int K_DELETE=32;
    public static final int T__149=149;
    public static final int LETTER=129;
    public static final int K_AS=8;
    public static final int K_FILTERING=15;
    public static final int K_SELECT=5;
    public static final int K_INTO=23;
    public static final int K_WRITETIME=16;
    public static final int K_AND=19;
    public static final int K_USERS=74;
    public static final int K_COLUMNFAMILY=41;
    public static final int K_TTL=17;
    public static final int K_GRANT=60;
    public static final int K_INT=95;
    public static final int K_COUNTER=35;
    public static final int K_AUTHORIZE=66;
    public static final int UUID=79;
    public static final int K_COUNT=7;
    public static final int INTEGER=18;
    public static final int K_TRIGGER=52;
    public static final int K_TIMESTAMP=29;
    public static final int K_NULL=83;
    public static final int K_WHERE=10;
    public static final int BOOLEAN=78;
    public static final int K_REVOKE=61;
    public static final int K_TIMEUUID=100;
    public static final int K_TOKEN=85;
    public static final int K_IF=25;
    public static final int K_DISTINCT=6;
    public static final int K_DOUBLE=92;
    public static final int K_PERMISSION=69;
    public static final int K_IN=86;
    public static final int K_ADD=56;
    public static final int K_ALL=67;
    public static final int FLOAT=77;
    public static final int K_FLOAT=93;
    public static final int K_UPDATE=30;
    public static final int K_BEGIN=33;
    public static final int K_TO=58;
    public static final int K_TRUNCATE=59;
    public static final int WS=132;
    public static final int EOF=-1;
    public static final int K_NOSUPERUSER=73;
    public static final int K_BY=12;
    public static final int DIGIT=128;
    public static final int K_EXISTS=27;
    public static final int K_NOT=26;
    public static final int K_TYPE=55;
    public static final int IDENT=49;
    public static final int K_RENAME=57;
    public static final int STRING_LITERAL=51;
    public static final int HEX=130;
    public static final int K_TEXT=96;
    public static final int K_KEYSPACE=39;

    // delegates
    // delegators


        public CqlParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CqlParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return CqlParser.tokenNames; }
    public String getGrammarFileName() { return "/home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g"; }


        private final List<String> recognitionErrors = new ArrayList<String>();
        private final List<ColumnIdentifier> bindVariables = new ArrayList<ColumnIdentifier>();

        public AbstractMarker.Raw newBindVariables(ColumnIdentifier name)
        {
            AbstractMarker.Raw marker = new AbstractMarker.Raw(bindVariables.size());
            bindVariables.add(name);
            return marker;
        }

        public AbstractMarker.INRaw newINBindVariables(ColumnIdentifier name)
        {
            AbstractMarker.INRaw marker = new AbstractMarker.INRaw(bindVariables.size());
            bindVariables.add(name);
            return marker;
        }

        public void displayRecognitionError(String[] tokenNames, RecognitionException e)
        {
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, tokenNames);
            recognitionErrors.add(hdr + " " + msg);
        }

        public void addRecognitionError(String msg)
        {
            recognitionErrors.add(msg);
        }

        public List<String> getRecognitionErrors()
        {
            return recognitionErrors;
        }

        public void throwLastRecognitionError() throws SyntaxException
        {
            if (recognitionErrors.size() > 0)
                throw new SyntaxException(recognitionErrors.get((recognitionErrors.size()-1)));
        }

        public Map<String, String> convertPropertyMap(Maps.Literal map)
        {
            if (map == null || map.entries == null || map.entries.isEmpty())
                return Collections.<String, String>emptyMap();

            Map<String, String> res = new HashMap<String, String>(map.entries.size());

            for (Pair<Term.Raw, Term.Raw> entry : map.entries)
            {
                // Because the parser tries to be smart and recover on error (to
                // allow displaying more than one error I suppose), we have null
                // entries in there. Just skip those, a proper error will be thrown in the end.
                if (entry.left == null || entry.right == null)
                    break;

                if (!(entry.left instanceof Constants.Literal))
                {
                    addRecognitionError("Invalid property name: " + entry.left);
                    break;
                }
                if (!(entry.right instanceof Constants.Literal))
                {
                    addRecognitionError("Invalid property value: " + entry.right);
                    break;
                }

                res.put(((Constants.Literal)entry.left).getRawText(), ((Constants.Literal)entry.right).getRawText());
            }

            return res;
        }

        public void addRawUpdate(List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations, ColumnIdentifier key, Operation.RawUpdate update)
        {
            for (Pair<ColumnIdentifier, Operation.RawUpdate> p : operations)
            {
                if (p.left.equals(key) && !p.right.isCompatibleWith(update))
                    addRecognitionError("Multiple incompatible setting of column " + key);
            }
            operations.add(Pair.create(key, update));
        }



    // $ANTLR start "query"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:180:1: query returns [ParsedStatement stmnt] : st= cqlStatement ( ';' )* EOF ;
    public final ParsedStatement query() throws RecognitionException {
        ParsedStatement stmnt = null;

        ParsedStatement st = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:183:5: (st= cqlStatement ( ';' )* EOF )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:183:7: st= cqlStatement ( ';' )* EOF
            {
            pushFollow(FOLLOW_cqlStatement_in_query72);
            st=cqlStatement();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:183:23: ( ';' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==135) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:183:24: ';'
            	    {
            	    match(input,135,FOLLOW_135_in_query75); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_query79); 
             stmnt = st; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmnt;
    }
    // $ANTLR end "query"


    // $ANTLR start "cqlStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:186:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement | st2= insertStatement | st3= updateStatement | st4= batchStatement | st5= deleteStatement | st6= useStatement | st7= truncateStatement | st8= createKeyspaceStatement | st9= createTableStatement | st10= createIndexStatement | st11= dropKeyspaceStatement | st12= dropTableStatement | st13= dropIndexStatement | st14= alterTableStatement | st15= alterKeyspaceStatement | st16= grantStatement | st17= revokeStatement | st18= listPermissionsStatement | st19= createUserStatement | st20= alterUserStatement | st21= dropUserStatement | st22= listUsersStatement | st23= createTriggerStatement | st24= dropTriggerStatement );
    public final ParsedStatement cqlStatement() throws RecognitionException {
        ParsedStatement stmt = null;

        SelectStatement.RawStatement st1 = null;

        UpdateStatement.ParsedInsert st2 = null;

        UpdateStatement.ParsedUpdate st3 = null;

        BatchStatement.Parsed st4 = null;

        DeleteStatement.Parsed st5 = null;

        UseStatement st6 = null;

        TruncateStatement st7 = null;

        CreateKeyspaceStatement st8 = null;

        CreateTableStatement.RawStatement st9 = null;

        CreateIndexStatement st10 = null;

        DropKeyspaceStatement st11 = null;

        DropTableStatement st12 = null;

        DropIndexStatement st13 = null;

        AlterTableStatement st14 = null;

        AlterKeyspaceStatement st15 = null;

        GrantStatement st16 = null;

        RevokeStatement st17 = null;

        ListPermissionsStatement st18 = null;

        CreateUserStatement st19 = null;

        AlterUserStatement st20 = null;

        DropUserStatement st21 = null;

        ListUsersStatement st22 = null;

        CreateTriggerStatement st23 = null;

        DropTriggerStatement st24 = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:188:5: (st1= selectStatement | st2= insertStatement | st3= updateStatement | st4= batchStatement | st5= deleteStatement | st6= useStatement | st7= truncateStatement | st8= createKeyspaceStatement | st9= createTableStatement | st10= createIndexStatement | st11= dropKeyspaceStatement | st12= dropTableStatement | st13= dropIndexStatement | st14= alterTableStatement | st15= alterKeyspaceStatement | st16= grantStatement | st17= revokeStatement | st18= listPermissionsStatement | st19= createUserStatement | st20= alterUserStatement | st21= dropUserStatement | st22= listUsersStatement | st23= createTriggerStatement | st24= dropTriggerStatement )
            int alt2=24;
            alt2 = dfa2.predict(input);
            switch (alt2) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:188:7: st1= selectStatement
                    {
                    pushFollow(FOLLOW_selectStatement_in_cqlStatement113);
                    st1=selectStatement();

                    state._fsp--;

                     stmt = st1; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:189:7: st2= insertStatement
                    {
                    pushFollow(FOLLOW_insertStatement_in_cqlStatement138);
                    st2=insertStatement();

                    state._fsp--;

                     stmt = st2; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:190:7: st3= updateStatement
                    {
                    pushFollow(FOLLOW_updateStatement_in_cqlStatement163);
                    st3=updateStatement();

                    state._fsp--;

                     stmt = st3; 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:191:7: st4= batchStatement
                    {
                    pushFollow(FOLLOW_batchStatement_in_cqlStatement188);
                    st4=batchStatement();

                    state._fsp--;

                     stmt = st4; 

                    }
                    break;
                case 5 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:192:7: st5= deleteStatement
                    {
                    pushFollow(FOLLOW_deleteStatement_in_cqlStatement214);
                    st5=deleteStatement();

                    state._fsp--;

                     stmt = st5; 

                    }
                    break;
                case 6 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:193:7: st6= useStatement
                    {
                    pushFollow(FOLLOW_useStatement_in_cqlStatement239);
                    st6=useStatement();

                    state._fsp--;

                     stmt = st6; 

                    }
                    break;
                case 7 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:194:7: st7= truncateStatement
                    {
                    pushFollow(FOLLOW_truncateStatement_in_cqlStatement267);
                    st7=truncateStatement();

                    state._fsp--;

                     stmt = st7; 

                    }
                    break;
                case 8 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:195:7: st8= createKeyspaceStatement
                    {
                    pushFollow(FOLLOW_createKeyspaceStatement_in_cqlStatement290);
                    st8=createKeyspaceStatement();

                    state._fsp--;

                     stmt = st8; 

                    }
                    break;
                case 9 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:196:7: st9= createTableStatement
                    {
                    pushFollow(FOLLOW_createTableStatement_in_cqlStatement307);
                    st9=createTableStatement();

                    state._fsp--;

                     stmt = st9; 

                    }
                    break;
                case 10 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:197:7: st10= createIndexStatement
                    {
                    pushFollow(FOLLOW_createIndexStatement_in_cqlStatement326);
                    st10=createIndexStatement();

                    state._fsp--;

                     stmt = st10; 

                    }
                    break;
                case 11 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:198:7: st11= dropKeyspaceStatement
                    {
                    pushFollow(FOLLOW_dropKeyspaceStatement_in_cqlStatement345);
                    st11=dropKeyspaceStatement();

                    state._fsp--;

                     stmt = st11; 

                    }
                    break;
                case 12 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:199:7: st12= dropTableStatement
                    {
                    pushFollow(FOLLOW_dropTableStatement_in_cqlStatement363);
                    st12=dropTableStatement();

                    state._fsp--;

                     stmt = st12; 

                    }
                    break;
                case 13 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:200:7: st13= dropIndexStatement
                    {
                    pushFollow(FOLLOW_dropIndexStatement_in_cqlStatement384);
                    st13=dropIndexStatement();

                    state._fsp--;

                     stmt = st13; 

                    }
                    break;
                case 14 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:201:7: st14= alterTableStatement
                    {
                    pushFollow(FOLLOW_alterTableStatement_in_cqlStatement405);
                    st14=alterTableStatement();

                    state._fsp--;

                     stmt = st14; 

                    }
                    break;
                case 15 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:202:7: st15= alterKeyspaceStatement
                    {
                    pushFollow(FOLLOW_alterKeyspaceStatement_in_cqlStatement425);
                    st15=alterKeyspaceStatement();

                    state._fsp--;

                     stmt = st15; 

                    }
                    break;
                case 16 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:203:7: st16= grantStatement
                    {
                    pushFollow(FOLLOW_grantStatement_in_cqlStatement442);
                    st16=grantStatement();

                    state._fsp--;

                     stmt = st16; 

                    }
                    break;
                case 17 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:204:7: st17= revokeStatement
                    {
                    pushFollow(FOLLOW_revokeStatement_in_cqlStatement467);
                    st17=revokeStatement();

                    state._fsp--;

                     stmt = st17; 

                    }
                    break;
                case 18 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:205:7: st18= listPermissionsStatement
                    {
                    pushFollow(FOLLOW_listPermissionsStatement_in_cqlStatement491);
                    st18=listPermissionsStatement();

                    state._fsp--;

                     stmt = st18; 

                    }
                    break;
                case 19 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:206:7: st19= createUserStatement
                    {
                    pushFollow(FOLLOW_createUserStatement_in_cqlStatement506);
                    st19=createUserStatement();

                    state._fsp--;

                     stmt = st19; 

                    }
                    break;
                case 20 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:207:7: st20= alterUserStatement
                    {
                    pushFollow(FOLLOW_alterUserStatement_in_cqlStatement526);
                    st20=alterUserStatement();

                    state._fsp--;

                     stmt = st20; 

                    }
                    break;
                case 21 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:208:7: st21= dropUserStatement
                    {
                    pushFollow(FOLLOW_dropUserStatement_in_cqlStatement547);
                    st21=dropUserStatement();

                    state._fsp--;

                     stmt = st21; 

                    }
                    break;
                case 22 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:209:7: st22= listUsersStatement
                    {
                    pushFollow(FOLLOW_listUsersStatement_in_cqlStatement569);
                    st22=listUsersStatement();

                    state._fsp--;

                     stmt = st22; 

                    }
                    break;
                case 23 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:210:7: st23= createTriggerStatement
                    {
                    pushFollow(FOLLOW_createTriggerStatement_in_cqlStatement590);
                    st23=createTriggerStatement();

                    state._fsp--;

                     stmt = st23; 

                    }
                    break;
                case 24 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:211:7: st24= dropTriggerStatement
                    {
                    pushFollow(FOLLOW_dropTriggerStatement_in_cqlStatement607);
                    st24=dropTriggerStatement();

                    state._fsp--;

                     stmt = st24; 

                    }
                    break;

            }
             if (stmt != null) stmt.setBoundVariables(bindVariables); 
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "cqlStatement"


    // $ANTLR start "useStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:217:1: useStatement returns [UseStatement stmt] : K_USE ks= keyspaceName ;
    public final UseStatement useStatement() throws RecognitionException {
        UseStatement stmt = null;

        String ks = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:218:5: ( K_USE ks= keyspaceName )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:218:7: K_USE ks= keyspaceName
            {
            match(input,K_USE,FOLLOW_K_USE_in_useStatement639); 
            pushFollow(FOLLOW_keyspaceName_in_useStatement643);
            ks=keyspaceName();

            state._fsp--;

             stmt = new UseStatement(ks); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "useStatement"


    // $ANTLR start "selectStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:221:1: selectStatement returns [SelectStatement.RawStatement expr] : K_SELECT ( ( K_DISTINCT )? sclause= selectClause | ( K_COUNT '(' sclause= selectCountClause ')' ( K_AS c= cident )? ) ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )? ;
    public final SelectStatement.RawStatement selectStatement() throws RecognitionException {
        SelectStatement.RawStatement expr = null;

        List<RawSelector> sclause = null;

        ColumnIdentifier c = null;

        CFName cf = null;

        List<Relation> wclause = null;

        Term.Raw rows = null;



                boolean isDistinct = false;
                boolean isCount = false;
                ColumnIdentifier countAlias = null;
                Term.Raw limit = null;
                Map<ColumnIdentifier, Boolean> orderings = new LinkedHashMap<ColumnIdentifier, Boolean>();
                boolean allowFiltering = false;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:5: ( K_SELECT ( ( K_DISTINCT )? sclause= selectClause | ( K_COUNT '(' sclause= selectCountClause ')' ( K_AS c= cident )? ) ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:7: K_SELECT ( ( K_DISTINCT )? sclause= selectClause | ( K_COUNT '(' sclause= selectCountClause ')' ( K_AS c= cident )? ) ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )?
            {
            match(input,K_SELECT,FOLLOW_K_SELECT_in_selectStatement677); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:16: ( ( K_DISTINCT )? sclause= selectClause | ( K_COUNT '(' sclause= selectCountClause ')' ( K_AS c= cident )? ) )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==K_DISTINCT||LA5_0==K_AS||(LA5_0>=K_FILTERING && LA5_0<=K_TTL)||LA5_0==K_VALUES||LA5_0==K_EXISTS||LA5_0==K_TIMESTAMP||LA5_0==K_COUNTER||(LA5_0>=K_KEY && LA5_0<=K_CUSTOM)||LA5_0==IDENT||LA5_0==K_TRIGGER||LA5_0==K_TYPE||LA5_0==K_LIST||(LA5_0>=K_ALL && LA5_0<=QUOTED_NAME)||LA5_0==K_TOKEN||(LA5_0>=K_ASCII && LA5_0<=K_MAP)||LA5_0==139) ) {
                alt5=1;
            }
            else if ( (LA5_0==K_COUNT) ) {
                int LA5_2 = input.LA(2);

                if ( (LA5_2==136) ) {
                    alt5=2;
                }
                else if ( ((LA5_2>=K_AS && LA5_2<=K_FROM)||LA5_2==138) ) {
                    alt5=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:18: ( K_DISTINCT )? sclause= selectClause
                    {
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:18: ( K_DISTINCT )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==K_DISTINCT) ) {
                        int LA3_1 = input.LA(2);

                        if ( ((LA3_1>=K_DISTINCT && LA3_1<=K_COUNT)||(LA3_1>=K_FILTERING && LA3_1<=K_TTL)||LA3_1==K_VALUES||LA3_1==K_EXISTS||LA3_1==K_TIMESTAMP||LA3_1==K_COUNTER||(LA3_1>=K_KEY && LA3_1<=K_CUSTOM)||LA3_1==IDENT||LA3_1==K_TRIGGER||LA3_1==K_TYPE||LA3_1==K_LIST||(LA3_1>=K_ALL && LA3_1<=QUOTED_NAME)||LA3_1==K_TOKEN||(LA3_1>=K_ASCII && LA3_1<=K_MAP)||LA3_1==139) ) {
                            alt3=1;
                        }
                        else if ( (LA3_1==K_AS) ) {
                            int LA3_4 = input.LA(3);

                            if ( (LA3_4==K_FROM||LA3_4==136||LA3_4==138) ) {
                                alt3=1;
                            }
                            else if ( (LA3_4==K_AS) ) {
                                int LA3_5 = input.LA(4);

                                if ( ((LA3_5>=K_DISTINCT && LA3_5<=K_AS)||(LA3_5>=K_FILTERING && LA3_5<=K_TTL)||LA3_5==K_VALUES||LA3_5==K_EXISTS||LA3_5==K_TIMESTAMP||LA3_5==K_COUNTER||(LA3_5>=K_KEY && LA3_5<=K_CUSTOM)||LA3_5==IDENT||LA3_5==K_TRIGGER||LA3_5==K_TYPE||LA3_5==K_LIST||(LA3_5>=K_ALL && LA3_5<=QUOTED_NAME)||(LA3_5>=K_ASCII && LA3_5<=K_MAP)) ) {
                                    alt3=1;
                                }
                            }
                        }
                    }
                    switch (alt3) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:20: K_DISTINCT
                            {
                            match(input,K_DISTINCT,FOLLOW_K_DISTINCT_in_selectStatement683); 
                             isDistinct = true; 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_selectClause_in_selectStatement692);
                    sclause=selectClause();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:18: ( K_COUNT '(' sclause= selectCountClause ')' ( K_AS c= cident )? )
                    {
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:18: ( K_COUNT '(' sclause= selectCountClause ')' ( K_AS c= cident )? )
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:19: K_COUNT '(' sclause= selectCountClause ')' ( K_AS c= cident )?
                    {
                    match(input,K_COUNT,FOLLOW_K_COUNT_in_selectStatement712); 
                    match(input,136,FOLLOW_136_in_selectStatement714); 
                    pushFollow(FOLLOW_selectCountClause_in_selectStatement718);
                    sclause=selectCountClause();

                    state._fsp--;

                    match(input,137,FOLLOW_137_in_selectStatement720); 
                     isCount = true; 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:81: ( K_AS c= cident )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==K_AS) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:82: K_AS c= cident
                            {
                            match(input,K_AS,FOLLOW_K_AS_in_selectStatement725); 
                            pushFollow(FOLLOW_cident_in_selectStatement729);
                            c=cident();

                            state._fsp--;

                             countAlias = c; 

                            }
                            break;

                    }


                    }


                    }
                    break;

            }

            match(input,K_FROM,FOLLOW_K_FROM_in_selectStatement744); 
            pushFollow(FOLLOW_columnFamilyName_in_selectStatement748);
            cf=columnFamilyName();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:239:7: ( K_WHERE wclause= whereClause )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==K_WHERE) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:239:9: K_WHERE wclause= whereClause
                    {
                    match(input,K_WHERE,FOLLOW_K_WHERE_in_selectStatement758); 
                    pushFollow(FOLLOW_whereClause_in_selectStatement762);
                    wclause=whereClause();

                    state._fsp--;


                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:7: ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==K_ORDER) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:9: K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )*
                    {
                    match(input,K_ORDER,FOLLOW_K_ORDER_in_selectStatement775); 
                    match(input,K_BY,FOLLOW_K_BY_in_selectStatement777); 
                    pushFollow(FOLLOW_orderByClause_in_selectStatement779);
                    orderByClause(orderings);

                    state._fsp--;

                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:47: ( ',' orderByClause[orderings] )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==138) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:49: ',' orderByClause[orderings]
                    	    {
                    	    match(input,138,FOLLOW_138_in_selectStatement784); 
                    	    pushFollow(FOLLOW_orderByClause_in_selectStatement786);
                    	    orderByClause(orderings);

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);


                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:241:7: ( K_LIMIT rows= intValue )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==K_LIMIT) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:241:9: K_LIMIT rows= intValue
                    {
                    match(input,K_LIMIT,FOLLOW_K_LIMIT_in_selectStatement803); 
                    pushFollow(FOLLOW_intValue_in_selectStatement807);
                    rows=intValue();

                    state._fsp--;

                     limit = rows; 

                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:242:7: ( K_ALLOW K_FILTERING )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==K_ALLOW) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:242:9: K_ALLOW K_FILTERING
                    {
                    match(input,K_ALLOW,FOLLOW_K_ALLOW_in_selectStatement822); 
                    match(input,K_FILTERING,FOLLOW_K_FILTERING_in_selectStatement824); 
                     allowFiltering = true; 

                    }
                    break;

            }


                      SelectStatement.Parameters params = new SelectStatement.Parameters(orderings,
                                                                                         isDistinct,
                                                                                         isCount,
                                                                                         countAlias,
                                                                                         allowFiltering);
                      expr = new SelectStatement.RawStatement(cf, params, sclause, wclause, limit);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "selectStatement"


    // $ANTLR start "selectClause"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:253:1: selectClause returns [List<RawSelector> expr] : (t1= selector ( ',' tN= selector )* | '\\*' );
    public final List<RawSelector> selectClause() throws RecognitionException {
        List<RawSelector> expr = null;

        RawSelector t1 = null;

        RawSelector tN = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:254:5: (t1= selector ( ',' tN= selector )* | '\\*' )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( ((LA12_0>=K_DISTINCT && LA12_0<=K_AS)||(LA12_0>=K_FILTERING && LA12_0<=K_TTL)||LA12_0==K_VALUES||LA12_0==K_EXISTS||LA12_0==K_TIMESTAMP||LA12_0==K_COUNTER||(LA12_0>=K_KEY && LA12_0<=K_CUSTOM)||LA12_0==IDENT||LA12_0==K_TRIGGER||LA12_0==K_TYPE||LA12_0==K_LIST||(LA12_0>=K_ALL && LA12_0<=QUOTED_NAME)||LA12_0==K_TOKEN||(LA12_0>=K_ASCII && LA12_0<=K_MAP)) ) {
                alt12=1;
            }
            else if ( (LA12_0==139) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:254:7: t1= selector ( ',' tN= selector )*
                    {
                    pushFollow(FOLLOW_selector_in_selectClause861);
                    t1=selector();

                    state._fsp--;

                     expr = new ArrayList<RawSelector>(); expr.add(t1); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:254:76: ( ',' tN= selector )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( (LA11_0==138) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:254:77: ',' tN= selector
                    	    {
                    	    match(input,138,FOLLOW_138_in_selectClause866); 
                    	    pushFollow(FOLLOW_selector_in_selectClause870);
                    	    tN=selector();

                    	    state._fsp--;

                    	     expr.add(tN); 

                    	    }
                    	    break;

                    	default :
                    	    break loop11;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:255:7: '\\*'
                    {
                    match(input,139,FOLLOW_139_in_selectClause882); 
                     expr = Collections.<RawSelector>emptyList();

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "selectClause"


    // $ANTLR start "selector"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:258:1: selector returns [RawSelector s] : us= unaliasedSelector ( K_AS c= cident )? ;
    public final RawSelector selector() throws RecognitionException {
        RawSelector s = null;

        Selectable us = null;

        ColumnIdentifier c = null;


         ColumnIdentifier alias = null; 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:5: (us= unaliasedSelector ( K_AS c= cident )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:7: us= unaliasedSelector ( K_AS c= cident )?
            {
            pushFollow(FOLLOW_unaliasedSelector_in_selector915);
            us=unaliasedSelector();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:28: ( K_AS c= cident )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==K_AS) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:29: K_AS c= cident
                    {
                    match(input,K_AS,FOLLOW_K_AS_in_selector918); 
                    pushFollow(FOLLOW_cident_in_selector922);
                    c=cident();

                    state._fsp--;

                     alias = c; 

                    }
                    break;

            }

             s = new RawSelector(us, alias); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "selector"


    // $ANTLR start "unaliasedSelector"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:263:1: unaliasedSelector returns [Selectable s] : (c= cident | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' | f= functionName args= selectionFunctionArgs );
    public final Selectable unaliasedSelector() throws RecognitionException {
        Selectable s = null;

        ColumnIdentifier c = null;

        String f = null;

        List<Selectable> args = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:264:5: (c= cident | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' | f= functionName args= selectionFunctionArgs )
            int alt14=4;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:264:7: c= cident
                    {
                    pushFollow(FOLLOW_cident_in_unaliasedSelector951);
                    c=cident();

                    state._fsp--;

                     s = c; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:265:7: K_WRITETIME '(' c= cident ')'
                    {
                    match(input,K_WRITETIME,FOLLOW_K_WRITETIME_in_unaliasedSelector994); 
                    match(input,136,FOLLOW_136_in_unaliasedSelector996); 
                    pushFollow(FOLLOW_cident_in_unaliasedSelector1000);
                    c=cident();

                    state._fsp--;

                    match(input,137,FOLLOW_137_in_unaliasedSelector1002); 
                     s = new Selectable.WritetimeOrTTL(c, true); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:266:7: K_TTL '(' c= cident ')'
                    {
                    match(input,K_TTL,FOLLOW_K_TTL_in_unaliasedSelector1025); 
                    match(input,136,FOLLOW_136_in_unaliasedSelector1033); 
                    pushFollow(FOLLOW_cident_in_unaliasedSelector1037);
                    c=cident();

                    state._fsp--;

                    match(input,137,FOLLOW_137_in_unaliasedSelector1039); 
                     s = new Selectable.WritetimeOrTTL(c, false); 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:267:7: f= functionName args= selectionFunctionArgs
                    {
                    pushFollow(FOLLOW_functionName_in_unaliasedSelector1064);
                    f=functionName();

                    state._fsp--;

                    pushFollow(FOLLOW_selectionFunctionArgs_in_unaliasedSelector1068);
                    args=selectionFunctionArgs();

                    state._fsp--;

                     s = new Selectable.WithFunction(f, args); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "unaliasedSelector"


    // $ANTLR start "selectionFunctionArgs"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:270:1: selectionFunctionArgs returns [List<Selectable> a] : ( '(' ')' | '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')' );
    public final List<Selectable> selectionFunctionArgs() throws RecognitionException {
        List<Selectable> a = null;

        Selectable s1 = null;

        Selectable sn = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:271:5: ( '(' ')' | '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')' )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==136) ) {
                int LA16_1 = input.LA(2);

                if ( (LA16_1==137) ) {
                    alt16=1;
                }
                else if ( ((LA16_1>=K_DISTINCT && LA16_1<=K_AS)||(LA16_1>=K_FILTERING && LA16_1<=K_TTL)||LA16_1==K_VALUES||LA16_1==K_EXISTS||LA16_1==K_TIMESTAMP||LA16_1==K_COUNTER||(LA16_1>=K_KEY && LA16_1<=K_CUSTOM)||LA16_1==IDENT||LA16_1==K_TRIGGER||LA16_1==K_TYPE||LA16_1==K_LIST||(LA16_1>=K_ALL && LA16_1<=QUOTED_NAME)||LA16_1==K_TOKEN||(LA16_1>=K_ASCII && LA16_1<=K_MAP)) ) {
                    alt16=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:271:7: '(' ')'
                    {
                    match(input,136,FOLLOW_136_in_selectionFunctionArgs1091); 
                    match(input,137,FOLLOW_137_in_selectionFunctionArgs1093); 
                     a = Collections.emptyList(); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:272:7: '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')'
                    {
                    match(input,136,FOLLOW_136_in_selectionFunctionArgs1103); 
                    pushFollow(FOLLOW_unaliasedSelector_in_selectionFunctionArgs1107);
                    s1=unaliasedSelector();

                    state._fsp--;

                     List<Selectable> args = new ArrayList<Selectable>(); args.add(s1); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:273:11: ( ',' sn= unaliasedSelector )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0==138) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:273:13: ',' sn= unaliasedSelector
                    	    {
                    	    match(input,138,FOLLOW_138_in_selectionFunctionArgs1123); 
                    	    pushFollow(FOLLOW_unaliasedSelector_in_selectionFunctionArgs1127);
                    	    sn=unaliasedSelector();

                    	    state._fsp--;

                    	     args.add(sn); 

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);

                    match(input,137,FOLLOW_137_in_selectionFunctionArgs1141); 
                     a = args; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return a;
    }
    // $ANTLR end "selectionFunctionArgs"


    // $ANTLR start "selectCountClause"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:277:1: selectCountClause returns [List<RawSelector> expr] : ( '\\*' | i= INTEGER );
    public final List<RawSelector> selectCountClause() throws RecognitionException {
        List<RawSelector> expr = null;

        Token i=null;

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:278:5: ( '\\*' | i= INTEGER )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==139) ) {
                alt17=1;
            }
            else if ( (LA17_0==INTEGER) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:278:7: '\\*'
                    {
                    match(input,139,FOLLOW_139_in_selectCountClause1164); 
                     expr = Collections.<RawSelector>emptyList();

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:279:7: i= INTEGER
                    {
                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_selectCountClause1186); 
                     if (!i.getText().equals("1")) addRecognitionError("Only COUNT(1) is supported, got COUNT(" + i.getText() + ")"); expr = Collections.<RawSelector>emptyList();

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "selectCountClause"


    // $ANTLR start "whereClause"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:282:1: whereClause returns [List<Relation> clause] : relation[$clause] ( K_AND relation[$clause] )* ;
    public final List<Relation> whereClause() throws RecognitionException {
        List<Relation> clause = null;

         clause = new ArrayList<Relation>(); 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:284:5: ( relation[$clause] ( K_AND relation[$clause] )* )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:284:7: relation[$clause] ( K_AND relation[$clause] )*
            {
            pushFollow(FOLLOW_relation_in_whereClause1222);
            relation(clause);

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:284:25: ( K_AND relation[$clause] )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==K_AND) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:284:26: K_AND relation[$clause]
            	    {
            	    match(input,K_AND,FOLLOW_K_AND_in_whereClause1226); 
            	    pushFollow(FOLLOW_relation_in_whereClause1228);
            	    relation(clause);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return clause;
    }
    // $ANTLR end "whereClause"


    // $ANTLR start "orderByClause"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:287:1: orderByClause[Map<ColumnIdentifier, Boolean> orderings] : c= cident ( K_ASC | K_DESC )? ;
    public final void orderByClause(Map<ColumnIdentifier, Boolean> orderings) throws RecognitionException {
        ColumnIdentifier c = null;



                ColumnIdentifier orderBy = null;
                boolean reversed = false;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:292:5: (c= cident ( K_ASC | K_DESC )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:292:7: c= cident ( K_ASC | K_DESC )?
            {
            pushFollow(FOLLOW_cident_in_orderByClause1259);
            c=cident();

            state._fsp--;

             orderBy = c; 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:292:33: ( K_ASC | K_DESC )?
            int alt19=3;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==K_ASC) ) {
                alt19=1;
            }
            else if ( (LA19_0==K_DESC) ) {
                alt19=2;
            }
            switch (alt19) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:292:34: K_ASC
                    {
                    match(input,K_ASC,FOLLOW_K_ASC_in_orderByClause1264); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:292:42: K_DESC
                    {
                    match(input,K_DESC,FOLLOW_K_DESC_in_orderByClause1268); 
                     reversed = true; 

                    }
                    break;

            }

             orderings.put(c, reversed); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "orderByClause"


    // $ANTLR start "insertStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:295:1: insertStatement returns [UpdateStatement.ParsedInsert expr] : K_INSERT K_INTO cf= columnFamilyName '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? ;
    public final UpdateStatement.ParsedInsert insertStatement() throws RecognitionException {
        UpdateStatement.ParsedInsert expr = null;

        CFName cf = null;

        ColumnIdentifier c1 = null;

        ColumnIdentifier cn = null;

        Term.Raw v1 = null;

        Term.Raw vn = null;



                Attributes.Raw attrs = new Attributes.Raw();
                List<ColumnIdentifier> columnNames  = new ArrayList<ColumnIdentifier>();
                List<Term.Raw> values = new ArrayList<Term.Raw>();
                boolean ifNotExists = false;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:308:5: ( K_INSERT K_INTO cf= columnFamilyName '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:308:7: K_INSERT K_INTO cf= columnFamilyName '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )?
            {
            match(input,K_INSERT,FOLLOW_K_INSERT_in_insertStatement1306); 
            match(input,K_INTO,FOLLOW_K_INTO_in_insertStatement1308); 
            pushFollow(FOLLOW_columnFamilyName_in_insertStatement1312);
            cf=columnFamilyName();

            state._fsp--;

            match(input,136,FOLLOW_136_in_insertStatement1324); 
            pushFollow(FOLLOW_cident_in_insertStatement1328);
            c1=cident();

            state._fsp--;

             columnNames.add(c1); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:309:51: ( ',' cn= cident )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==138) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:309:53: ',' cn= cident
            	    {
            	    match(input,138,FOLLOW_138_in_insertStatement1335); 
            	    pushFollow(FOLLOW_cident_in_insertStatement1339);
            	    cn=cident();

            	    state._fsp--;

            	     columnNames.add(cn); 

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            match(input,137,FOLLOW_137_in_insertStatement1346); 
            match(input,K_VALUES,FOLLOW_K_VALUES_in_insertStatement1356); 
            match(input,136,FOLLOW_136_in_insertStatement1368); 
            pushFollow(FOLLOW_term_in_insertStatement1372);
            v1=term();

            state._fsp--;

             values.add(v1); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:311:43: ( ',' vn= term )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==138) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:311:45: ',' vn= term
            	    {
            	    match(input,138,FOLLOW_138_in_insertStatement1378); 
            	    pushFollow(FOLLOW_term_in_insertStatement1382);
            	    vn=term();

            	    state._fsp--;

            	     values.add(vn); 

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            match(input,137,FOLLOW_137_in_insertStatement1389); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:313:9: ( K_IF K_NOT K_EXISTS )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==K_IF) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:313:11: K_IF K_NOT K_EXISTS
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_insertStatement1402); 
                    match(input,K_NOT,FOLLOW_K_NOT_in_insertStatement1404); 
                    match(input,K_EXISTS,FOLLOW_K_EXISTS_in_insertStatement1406); 
                     ifNotExists = true; 

                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:314:9: ( usingClause[attrs] )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==K_USING) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:314:11: usingClause[attrs]
                    {
                    pushFollow(FOLLOW_usingClause_in_insertStatement1423);
                    usingClause(attrs);

                    state._fsp--;


                    }
                    break;

            }


                      expr = new UpdateStatement.ParsedInsert(cf,
                                                               attrs,
                                                               columnNames,
                                                               values,
                                                               ifNotExists);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "insertStatement"


    // $ANTLR start "usingClause"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:324:1: usingClause[Attributes.Raw attrs] : K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )* ;
    public final void usingClause(Attributes.Raw attrs) throws RecognitionException {
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:5: ( K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )* )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:7: K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )*
            {
            match(input,K_USING,FOLLOW_K_USING_in_usingClause1453); 
            pushFollow(FOLLOW_usingClauseObjective_in_usingClause1455);
            usingClauseObjective(attrs);

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:43: ( K_AND usingClauseObjective[attrs] )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==K_AND) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:45: K_AND usingClauseObjective[attrs]
            	    {
            	    match(input,K_AND,FOLLOW_K_AND_in_usingClause1460); 
            	    pushFollow(FOLLOW_usingClauseObjective_in_usingClause1462);
            	    usingClauseObjective(attrs);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "usingClause"


    // $ANTLR start "usingClauseObjective"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:328:1: usingClauseObjective[Attributes.Raw attrs] : ( K_TIMESTAMP ts= intValue | K_TTL t= intValue );
    public final void usingClauseObjective(Attributes.Raw attrs) throws RecognitionException {
        Term.Raw ts = null;

        Term.Raw t = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:329:5: ( K_TIMESTAMP ts= intValue | K_TTL t= intValue )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==K_TIMESTAMP) ) {
                alt25=1;
            }
            else if ( (LA25_0==K_TTL) ) {
                alt25=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:329:7: K_TIMESTAMP ts= intValue
                    {
                    match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_usingClauseObjective1484); 
                    pushFollow(FOLLOW_intValue_in_usingClauseObjective1488);
                    ts=intValue();

                    state._fsp--;

                     attrs.timestamp = ts; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:330:7: K_TTL t= intValue
                    {
                    match(input,K_TTL,FOLLOW_K_TTL_in_usingClauseObjective1498); 
                    pushFollow(FOLLOW_intValue_in_usingClauseObjective1502);
                    t=intValue();

                    state._fsp--;

                     attrs.timeToLive = t; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "usingClauseObjective"


    // $ANTLR start "updateStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:333:1: updateStatement returns [UpdateStatement.ParsedUpdate expr] : K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF conditions= updateCondition )? ;
    public final UpdateStatement.ParsedUpdate updateStatement() throws RecognitionException {
        UpdateStatement.ParsedUpdate expr = null;

        CFName cf = null;

        List<Relation> wclause = null;

        List<Pair<ColumnIdentifier, Operation.RawUpdate>> conditions = null;



                Attributes.Raw attrs = new Attributes.Raw();
                List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations = new ArrayList<Pair<ColumnIdentifier, Operation.RawUpdate>>();
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:344:5: ( K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF conditions= updateCondition )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:344:7: K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF conditions= updateCondition )?
            {
            match(input,K_UPDATE,FOLLOW_K_UPDATE_in_updateStatement1536); 
            pushFollow(FOLLOW_columnFamilyName_in_updateStatement1540);
            cf=columnFamilyName();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:345:7: ( usingClause[attrs] )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==K_USING) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:345:9: usingClause[attrs]
                    {
                    pushFollow(FOLLOW_usingClause_in_updateStatement1550);
                    usingClause(attrs);

                    state._fsp--;


                    }
                    break;

            }

            match(input,K_SET,FOLLOW_K_SET_in_updateStatement1562); 
            pushFollow(FOLLOW_columnOperation_in_updateStatement1564);
            columnOperation(operations);

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:346:41: ( ',' columnOperation[operations] )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==138) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:346:42: ',' columnOperation[operations]
            	    {
            	    match(input,138,FOLLOW_138_in_updateStatement1568); 
            	    pushFollow(FOLLOW_columnOperation_in_updateStatement1570);
            	    columnOperation(operations);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);

            match(input,K_WHERE,FOLLOW_K_WHERE_in_updateStatement1581); 
            pushFollow(FOLLOW_whereClause_in_updateStatement1585);
            wclause=whereClause();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:348:7: ( K_IF conditions= updateCondition )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==K_IF) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:348:9: K_IF conditions= updateCondition
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_updateStatement1595); 
                    pushFollow(FOLLOW_updateCondition_in_updateStatement1599);
                    conditions=updateCondition();

                    state._fsp--;


                    }
                    break;

            }


                      return new UpdateStatement.ParsedUpdate(cf,
                                                              attrs,
                                                              operations,
                                                              wclause,
                                                              conditions == null ? Collections.<Pair<ColumnIdentifier, Operation.RawUpdate>>emptyList() : conditions);
                 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "updateStatement"


    // $ANTLR start "updateCondition"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:358:1: updateCondition returns [List<Pair<ColumnIdentifier, Operation.RawUpdate>> conditions] : columnOperation[conditions] ( K_AND columnOperation[conditions] )* ;
    public final List<Pair<ColumnIdentifier, Operation.RawUpdate>> updateCondition() throws RecognitionException {
        List<Pair<ColumnIdentifier, Operation.RawUpdate>> conditions = null;

         conditions = new ArrayList<Pair<ColumnIdentifier, Operation.RawUpdate>>(); 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:360:5: ( columnOperation[conditions] ( K_AND columnOperation[conditions] )* )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:360:7: columnOperation[conditions] ( K_AND columnOperation[conditions] )*
            {
            pushFollow(FOLLOW_columnOperation_in_updateCondition1640);
            columnOperation(conditions);

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:360:35: ( K_AND columnOperation[conditions] )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==K_AND) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:360:37: K_AND columnOperation[conditions]
            	    {
            	    match(input,K_AND,FOLLOW_K_AND_in_updateCondition1645); 
            	    pushFollow(FOLLOW_columnOperation_in_updateCondition1647);
            	    columnOperation(conditions);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return conditions;
    }
    // $ANTLR end "updateCondition"


    // $ANTLR start "deleteStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:363:1: deleteStatement returns [DeleteStatement.Parsed expr] : K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF conditions= updateCondition )? ;
    public final DeleteStatement.Parsed deleteStatement() throws RecognitionException {
        DeleteStatement.Parsed expr = null;

        List<Operation.RawDeletion> dels = null;

        CFName cf = null;

        List<Relation> wclause = null;

        List<Pair<ColumnIdentifier, Operation.RawUpdate>> conditions = null;



                Attributes.Raw attrs = new Attributes.Raw();
                List<Operation.RawDeletion> columnDeletions = Collections.emptyList();
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:374:5: ( K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF conditions= updateCondition )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:374:7: K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF conditions= updateCondition )?
            {
            match(input,K_DELETE,FOLLOW_K_DELETE_in_deleteStatement1683); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:374:16: (dels= deleteSelection )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( ((LA30_0>=K_DISTINCT && LA30_0<=K_AS)||(LA30_0>=K_FILTERING && LA30_0<=K_TTL)||LA30_0==K_VALUES||LA30_0==K_EXISTS||LA30_0==K_TIMESTAMP||LA30_0==K_COUNTER||(LA30_0>=K_KEY && LA30_0<=K_CUSTOM)||LA30_0==IDENT||LA30_0==K_TRIGGER||LA30_0==K_TYPE||LA30_0==K_LIST||(LA30_0>=K_ALL && LA30_0<=QUOTED_NAME)||(LA30_0>=K_ASCII && LA30_0<=K_MAP)) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:374:18: dels= deleteSelection
                    {
                    pushFollow(FOLLOW_deleteSelection_in_deleteStatement1689);
                    dels=deleteSelection();

                    state._fsp--;

                     columnDeletions = dels; 

                    }
                    break;

            }

            match(input,K_FROM,FOLLOW_K_FROM_in_deleteStatement1702); 
            pushFollow(FOLLOW_columnFamilyName_in_deleteStatement1706);
            cf=columnFamilyName();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:376:7: ( usingClauseDelete[attrs] )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==K_USING) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:376:9: usingClauseDelete[attrs]
                    {
                    pushFollow(FOLLOW_usingClauseDelete_in_deleteStatement1716);
                    usingClauseDelete(attrs);

                    state._fsp--;


                    }
                    break;

            }

            match(input,K_WHERE,FOLLOW_K_WHERE_in_deleteStatement1728); 
            pushFollow(FOLLOW_whereClause_in_deleteStatement1732);
            wclause=whereClause();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:378:7: ( K_IF conditions= updateCondition )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==K_IF) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:378:9: K_IF conditions= updateCondition
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_deleteStatement1742); 
                    pushFollow(FOLLOW_updateCondition_in_deleteStatement1746);
                    conditions=updateCondition();

                    state._fsp--;


                    }
                    break;

            }


                      return new DeleteStatement.Parsed(cf,
                                                        attrs,
                                                        columnDeletions,
                                                        wclause,
                                                        conditions == null ? Collections.<Pair<ColumnIdentifier, Operation.RawUpdate>>emptyList() : conditions);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "deleteStatement"


    // $ANTLR start "deleteSelection"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:388:1: deleteSelection returns [List<Operation.RawDeletion> operations] : t1= deleteOp ( ',' tN= deleteOp )* ;
    public final List<Operation.RawDeletion> deleteSelection() throws RecognitionException {
        List<Operation.RawDeletion> operations = null;

        Operation.RawDeletion t1 = null;

        Operation.RawDeletion tN = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:389:5: (t1= deleteOp ( ',' tN= deleteOp )* )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:389:7: t1= deleteOp ( ',' tN= deleteOp )*
            {
             operations = new ArrayList<Operation.RawDeletion>(); 
            pushFollow(FOLLOW_deleteOp_in_deleteSelection1792);
            t1=deleteOp();

            state._fsp--;

             operations.add(t1); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:11: ( ',' tN= deleteOp )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==138) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:12: ',' tN= deleteOp
            	    {
            	    match(input,138,FOLLOW_138_in_deleteSelection1807); 
            	    pushFollow(FOLLOW_deleteOp_in_deleteSelection1811);
            	    tN=deleteOp();

            	    state._fsp--;

            	     operations.add(tN); 

            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return operations;
    }
    // $ANTLR end "deleteSelection"


    // $ANTLR start "deleteOp"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:394:1: deleteOp returns [Operation.RawDeletion op] : (c= cident | c= cident '[' t= term ']' );
    public final Operation.RawDeletion deleteOp() throws RecognitionException {
        Operation.RawDeletion op = null;

        ColumnIdentifier c = null;

        Term.Raw t = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:395:5: (c= cident | c= cident '[' t= term ']' )
            int alt34=2;
            alt34 = dfa34.predict(input);
            switch (alt34) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:395:7: c= cident
                    {
                    pushFollow(FOLLOW_cident_in_deleteOp1838);
                    c=cident();

                    state._fsp--;

                     op = new Operation.ColumnDeletion(c); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:396:7: c= cident '[' t= term ']'
                    {
                    pushFollow(FOLLOW_cident_in_deleteOp1865);
                    c=cident();

                    state._fsp--;

                    match(input,140,FOLLOW_140_in_deleteOp1867); 
                    pushFollow(FOLLOW_term_in_deleteOp1871);
                    t=term();

                    state._fsp--;

                    match(input,141,FOLLOW_141_in_deleteOp1873); 
                     op = new Operation.ElementDeletion(c, t); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return op;
    }
    // $ANTLR end "deleteOp"


    // $ANTLR start "usingClauseDelete"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:399:1: usingClauseDelete[Attributes.Raw attrs] : K_USING K_TIMESTAMP ts= intValue ;
    public final void usingClauseDelete(Attributes.Raw attrs) throws RecognitionException {
        Term.Raw ts = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:400:5: ( K_USING K_TIMESTAMP ts= intValue )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:400:7: K_USING K_TIMESTAMP ts= intValue
            {
            match(input,K_USING,FOLLOW_K_USING_in_usingClauseDelete1893); 
            match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_usingClauseDelete1895); 
            pushFollow(FOLLOW_intValue_in_usingClauseDelete1899);
            ts=intValue();

            state._fsp--;

             attrs.timestamp = ts; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "usingClauseDelete"


    // $ANTLR start "batchStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:403:1: batchStatement returns [BatchStatement.Parsed expr] : K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH ;
    public final BatchStatement.Parsed batchStatement() throws RecognitionException {
        BatchStatement.Parsed expr = null;

        ModificationStatement.Parsed s = null;



                BatchStatement.Type type = BatchStatement.Type.LOGGED;
                List<ModificationStatement.Parsed> statements = new ArrayList<ModificationStatement.Parsed>();
                Attributes.Raw attrs = new Attributes.Raw();
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:433:5: ( K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:433:7: K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH
            {
            match(input,K_BEGIN,FOLLOW_K_BEGIN_in_batchStatement1933); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:434:7: ( K_UNLOGGED | K_COUNTER )?
            int alt35=3;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==K_UNLOGGED) ) {
                alt35=1;
            }
            else if ( (LA35_0==K_COUNTER) ) {
                alt35=2;
            }
            switch (alt35) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:434:9: K_UNLOGGED
                    {
                    match(input,K_UNLOGGED,FOLLOW_K_UNLOGGED_in_batchStatement1943); 
                     type = BatchStatement.Type.UNLOGGED; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:434:63: K_COUNTER
                    {
                    match(input,K_COUNTER,FOLLOW_K_COUNTER_in_batchStatement1949); 
                     type = BatchStatement.Type.COUNTER; 

                    }
                    break;

            }

            match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement1962); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:435:15: ( usingClause[attrs] )?
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==K_USING) ) {
                alt36=1;
            }
            switch (alt36) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:435:17: usingClause[attrs]
                    {
                    pushFollow(FOLLOW_usingClause_in_batchStatement1966);
                    usingClause(attrs);

                    state._fsp--;


                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:436:11: (s= batchStatementObjective ( ';' )? )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==K_INSERT||LA38_0==K_UPDATE||LA38_0==K_DELETE) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:436:13: s= batchStatementObjective ( ';' )?
            	    {
            	    pushFollow(FOLLOW_batchStatementObjective_in_batchStatement1986);
            	    s=batchStatementObjective();

            	    state._fsp--;

            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:436:39: ( ';' )?
            	    int alt37=2;
            	    int LA37_0 = input.LA(1);

            	    if ( (LA37_0==135) ) {
            	        alt37=1;
            	    }
            	    switch (alt37) {
            	        case 1 :
            	            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:436:39: ';'
            	            {
            	            match(input,135,FOLLOW_135_in_batchStatement1988); 

            	            }
            	            break;

            	    }

            	     statements.add(s); 

            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);

            match(input,K_APPLY,FOLLOW_K_APPLY_in_batchStatement2002); 
            match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement2004); 

                      return new BatchStatement.Parsed(type, attrs, statements);
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "batchStatement"


    // $ANTLR start "batchStatementObjective"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:443:1: batchStatementObjective returns [ModificationStatement.Parsed statement] : (i= insertStatement | u= updateStatement | d= deleteStatement );
    public final ModificationStatement.Parsed batchStatementObjective() throws RecognitionException {
        ModificationStatement.Parsed statement = null;

        UpdateStatement.ParsedInsert i = null;

        UpdateStatement.ParsedUpdate u = null;

        DeleteStatement.Parsed d = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:444:5: (i= insertStatement | u= updateStatement | d= deleteStatement )
            int alt39=3;
            switch ( input.LA(1) ) {
            case K_INSERT:
                {
                alt39=1;
                }
                break;
            case K_UPDATE:
                {
                alt39=2;
                }
                break;
            case K_DELETE:
                {
                alt39=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }

            switch (alt39) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:444:7: i= insertStatement
                    {
                    pushFollow(FOLLOW_insertStatement_in_batchStatementObjective2035);
                    i=insertStatement();

                    state._fsp--;

                     statement = i; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:7: u= updateStatement
                    {
                    pushFollow(FOLLOW_updateStatement_in_batchStatementObjective2048);
                    u=updateStatement();

                    state._fsp--;

                     statement = u; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:446:7: d= deleteStatement
                    {
                    pushFollow(FOLLOW_deleteStatement_in_batchStatementObjective2061);
                    d=deleteStatement();

                    state._fsp--;

                     statement = d; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return statement;
    }
    // $ANTLR end "batchStatementObjective"


    // $ANTLR start "createKeyspaceStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:449:1: createKeyspaceStatement returns [CreateKeyspaceStatement expr] : K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs] ;
    public final CreateKeyspaceStatement createKeyspaceStatement() throws RecognitionException {
        CreateKeyspaceStatement expr = null;

        String ks = null;



                KSPropDefs attrs = new KSPropDefs();
                boolean ifNotExists = false;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:457:5: ( K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs] )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:457:7: K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs]
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createKeyspaceStatement2096); 
            match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_createKeyspaceStatement2098); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:457:27: ( K_IF K_NOT K_EXISTS )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==K_IF) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:457:28: K_IF K_NOT K_EXISTS
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_createKeyspaceStatement2101); 
                    match(input,K_NOT,FOLLOW_K_NOT_in_createKeyspaceStatement2103); 
                    match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createKeyspaceStatement2105); 
                     ifNotExists = true; 

                    }
                    break;

            }

            pushFollow(FOLLOW_keyspaceName_in_createKeyspaceStatement2114);
            ks=keyspaceName();

            state._fsp--;

            match(input,K_WITH,FOLLOW_K_WITH_in_createKeyspaceStatement2122); 
            pushFollow(FOLLOW_properties_in_createKeyspaceStatement2124);
            properties(attrs);

            state._fsp--;

             expr = new CreateKeyspaceStatement(ks, attrs, ifNotExists); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createKeyspaceStatement"


    // $ANTLR start "createTableStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:461:1: createTableStatement returns [CreateTableStatement.RawStatement expr] : K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr] ;
    public final CreateTableStatement.RawStatement createTableStatement() throws RecognitionException {
        CreateTableStatement.RawStatement expr = null;

        CFName cf = null;


         boolean ifNotExists = false; 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:470:5: ( K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr] )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:470:7: K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr]
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createTableStatement2159); 
            match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_createTableStatement2161); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:470:31: ( K_IF K_NOT K_EXISTS )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==K_IF) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:470:32: K_IF K_NOT K_EXISTS
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_createTableStatement2164); 
                    match(input,K_NOT,FOLLOW_K_NOT_in_createTableStatement2166); 
                    match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createTableStatement2168); 
                     ifNotExists = true; 

                    }
                    break;

            }

            pushFollow(FOLLOW_columnFamilyName_in_createTableStatement2183);
            cf=columnFamilyName();

            state._fsp--;

             expr = new CreateTableStatement.RawStatement(cf, ifNotExists); 
            pushFollow(FOLLOW_cfamDefinition_in_createTableStatement2193);
            cfamDefinition(expr);

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createTableStatement"


    // $ANTLR start "cfamDefinition"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:475:1: cfamDefinition[CreateTableStatement.RawStatement expr] : '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )? ;
    public final void cfamDefinition(CreateTableStatement.RawStatement expr) throws RecognitionException {
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:476:5: ( '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:476:7: '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )?
            {
            match(input,136,FOLLOW_136_in_cfamDefinition2212); 
            pushFollow(FOLLOW_cfamColumns_in_cfamDefinition2214);
            cfamColumns(expr);

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:476:29: ( ',' ( cfamColumns[expr] )? )*
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==138) ) {
                    alt43=1;
                }


                switch (alt43) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:476:31: ',' ( cfamColumns[expr] )?
            	    {
            	    match(input,138,FOLLOW_138_in_cfamDefinition2219); 
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:476:35: ( cfamColumns[expr] )?
            	    int alt42=2;
            	    int LA42_0 = input.LA(1);

            	    if ( ((LA42_0>=K_DISTINCT && LA42_0<=K_AS)||(LA42_0>=K_FILTERING && LA42_0<=K_TTL)||LA42_0==K_VALUES||LA42_0==K_EXISTS||LA42_0==K_TIMESTAMP||LA42_0==K_COUNTER||(LA42_0>=K_PRIMARY && LA42_0<=K_CUSTOM)||LA42_0==IDENT||LA42_0==K_TRIGGER||LA42_0==K_TYPE||LA42_0==K_LIST||(LA42_0>=K_ALL && LA42_0<=QUOTED_NAME)||(LA42_0>=K_ASCII && LA42_0<=K_MAP)) ) {
            	        alt42=1;
            	    }
            	    switch (alt42) {
            	        case 1 :
            	            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:476:35: cfamColumns[expr]
            	            {
            	            pushFollow(FOLLOW_cfamColumns_in_cfamDefinition2221);
            	            cfamColumns(expr);

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop43;
                }
            } while (true);

            match(input,137,FOLLOW_137_in_cfamDefinition2228); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:7: ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==K_WITH) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:9: K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )*
                    {
                    match(input,K_WITH,FOLLOW_K_WITH_in_cfamDefinition2238); 
                    pushFollow(FOLLOW_cfamProperty_in_cfamDefinition2240);
                    cfamProperty(expr);

                    state._fsp--;

                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:35: ( K_AND cfamProperty[expr] )*
                    loop44:
                    do {
                        int alt44=2;
                        int LA44_0 = input.LA(1);

                        if ( (LA44_0==K_AND) ) {
                            alt44=1;
                        }


                        switch (alt44) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:37: K_AND cfamProperty[expr]
                    	    {
                    	    match(input,K_AND,FOLLOW_K_AND_in_cfamDefinition2245); 
                    	    pushFollow(FOLLOW_cfamProperty_in_cfamDefinition2247);
                    	    cfamProperty(expr);

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfamDefinition"


    // $ANTLR start "cfamColumns"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:480:1: cfamColumns[CreateTableStatement.RawStatement expr] : (k= cident v= comparatorType ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= cident )* ')' );
    public final void cfamColumns(CreateTableStatement.RawStatement expr) throws RecognitionException {
        ColumnIdentifier k = null;

        CQL3Type v = null;

        ColumnIdentifier c = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:5: (k= cident v= comparatorType ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= cident )* ')' )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( ((LA48_0>=K_DISTINCT && LA48_0<=K_AS)||(LA48_0>=K_FILTERING && LA48_0<=K_TTL)||LA48_0==K_VALUES||LA48_0==K_EXISTS||LA48_0==K_TIMESTAMP||LA48_0==K_COUNTER||(LA48_0>=K_KEY && LA48_0<=K_CUSTOM)||LA48_0==IDENT||LA48_0==K_TRIGGER||LA48_0==K_TYPE||LA48_0==K_LIST||(LA48_0>=K_ALL && LA48_0<=QUOTED_NAME)||(LA48_0>=K_ASCII && LA48_0<=K_MAP)) ) {
                alt48=1;
            }
            else if ( (LA48_0==K_PRIMARY) ) {
                alt48=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }
            switch (alt48) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:7: k= cident v= comparatorType ( K_PRIMARY K_KEY )?
                    {
                    pushFollow(FOLLOW_cident_in_cfamColumns2273);
                    k=cident();

                    state._fsp--;

                    pushFollow(FOLLOW_comparatorType_in_cfamColumns2277);
                    v=comparatorType();

                    state._fsp--;

                     expr.addDefinition(k, v); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:64: ( K_PRIMARY K_KEY )?
                    int alt46=2;
                    int LA46_0 = input.LA(1);

                    if ( (LA46_0==K_PRIMARY) ) {
                        alt46=1;
                    }
                    switch (alt46) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:65: K_PRIMARY K_KEY
                            {
                            match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns2282); 
                            match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns2284); 
                             expr.addKeyAliases(Collections.singletonList(k)); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:482:7: K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= cident )* ')'
                    {
                    match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns2296); 
                    match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns2298); 
                    match(input,136,FOLLOW_136_in_cfamColumns2300); 
                    pushFollow(FOLLOW_pkDef_in_cfamColumns2302);
                    pkDef(expr);

                    state._fsp--;

                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:482:39: ( ',' c= cident )*
                    loop47:
                    do {
                        int alt47=2;
                        int LA47_0 = input.LA(1);

                        if ( (LA47_0==138) ) {
                            alt47=1;
                        }


                        switch (alt47) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:482:40: ',' c= cident
                    	    {
                    	    match(input,138,FOLLOW_138_in_cfamColumns2306); 
                    	    pushFollow(FOLLOW_cident_in_cfamColumns2310);
                    	    c=cident();

                    	    state._fsp--;

                    	     expr.addColumnAlias(c); 

                    	    }
                    	    break;

                    	default :
                    	    break loop47;
                        }
                    } while (true);

                    match(input,137,FOLLOW_137_in_cfamColumns2317); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfamColumns"


    // $ANTLR start "pkDef"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:485:1: pkDef[CreateTableStatement.RawStatement expr] : (k= cident | '(' k1= cident ( ',' kn= cident )* ')' );
    public final void pkDef(CreateTableStatement.RawStatement expr) throws RecognitionException {
        ColumnIdentifier k = null;

        ColumnIdentifier k1 = null;

        ColumnIdentifier kn = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:486:5: (k= cident | '(' k1= cident ( ',' kn= cident )* ')' )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( ((LA50_0>=K_DISTINCT && LA50_0<=K_AS)||(LA50_0>=K_FILTERING && LA50_0<=K_TTL)||LA50_0==K_VALUES||LA50_0==K_EXISTS||LA50_0==K_TIMESTAMP||LA50_0==K_COUNTER||(LA50_0>=K_KEY && LA50_0<=K_CUSTOM)||LA50_0==IDENT||LA50_0==K_TRIGGER||LA50_0==K_TYPE||LA50_0==K_LIST||(LA50_0>=K_ALL && LA50_0<=QUOTED_NAME)||(LA50_0>=K_ASCII && LA50_0<=K_MAP)) ) {
                alt50=1;
            }
            else if ( (LA50_0==136) ) {
                alt50=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:486:7: k= cident
                    {
                    pushFollow(FOLLOW_cident_in_pkDef2337);
                    k=cident();

                    state._fsp--;

                     expr.addKeyAliases(Collections.singletonList(k)); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:487:7: '(' k1= cident ( ',' kn= cident )* ')'
                    {
                    match(input,136,FOLLOW_136_in_pkDef2347); 
                     List<ColumnIdentifier> l = new ArrayList<ColumnIdentifier>(); 
                    pushFollow(FOLLOW_cident_in_pkDef2353);
                    k1=cident();

                    state._fsp--;

                     l.add(k1); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:487:102: ( ',' kn= cident )*
                    loop49:
                    do {
                        int alt49=2;
                        int LA49_0 = input.LA(1);

                        if ( (LA49_0==138) ) {
                            alt49=1;
                        }


                        switch (alt49) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:487:104: ',' kn= cident
                    	    {
                    	    match(input,138,FOLLOW_138_in_pkDef2359); 
                    	    pushFollow(FOLLOW_cident_in_pkDef2363);
                    	    kn=cident();

                    	    state._fsp--;

                    	     l.add(kn); 

                    	    }
                    	    break;

                    	default :
                    	    break loop49;
                        }
                    } while (true);

                    match(input,137,FOLLOW_137_in_pkDef2370); 
                     expr.addKeyAliases(l); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "pkDef"


    // $ANTLR start "cfamProperty"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:490:1: cfamProperty[CreateTableStatement.RawStatement expr] : ( property[expr.properties] | K_COMPACT K_STORAGE | K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[expr] ( ',' cfamOrdering[expr] )* ')' );
    public final void cfamProperty(CreateTableStatement.RawStatement expr) throws RecognitionException {
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:491:5: ( property[expr.properties] | K_COMPACT K_STORAGE | K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[expr] ( ',' cfamOrdering[expr] )* ')' )
            int alt52=3;
            switch ( input.LA(1) ) {
            case K_DISTINCT:
            case K_COUNT:
            case K_AS:
            case K_FILTERING:
            case K_WRITETIME:
            case K_TTL:
            case K_VALUES:
            case K_EXISTS:
            case K_TIMESTAMP:
            case K_COUNTER:
            case K_KEY:
            case K_STORAGE:
            case K_CUSTOM:
            case IDENT:
            case K_TRIGGER:
            case K_TYPE:
            case K_LIST:
            case K_ALL:
            case K_PERMISSIONS:
            case K_PERMISSION:
            case K_KEYSPACES:
            case K_USER:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_USERS:
            case K_PASSWORD:
            case QUOTED_NAME:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
                {
                alt52=1;
                }
                break;
            case K_COMPACT:
                {
                int LA52_2 = input.LA(2);

                if ( (LA52_2==K_STORAGE) ) {
                    alt52=2;
                }
                else if ( (LA52_2==147) ) {
                    alt52=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 52, 2, input);

                    throw nvae;
                }
                }
                break;
            case K_CLUSTERING:
                {
                int LA52_3 = input.LA(2);

                if ( (LA52_3==K_ORDER) ) {
                    alt52=3;
                }
                else if ( (LA52_3==147) ) {
                    alt52=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 52, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;
            }

            switch (alt52) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:491:7: property[expr.properties]
                    {
                    pushFollow(FOLLOW_property_in_cfamProperty2390);
                    property(expr.properties);

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:492:7: K_COMPACT K_STORAGE
                    {
                    match(input,K_COMPACT,FOLLOW_K_COMPACT_in_cfamProperty2399); 
                    match(input,K_STORAGE,FOLLOW_K_STORAGE_in_cfamProperty2401); 
                     expr.setCompactStorage(); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:493:7: K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[expr] ( ',' cfamOrdering[expr] )* ')'
                    {
                    match(input,K_CLUSTERING,FOLLOW_K_CLUSTERING_in_cfamProperty2411); 
                    match(input,K_ORDER,FOLLOW_K_ORDER_in_cfamProperty2413); 
                    match(input,K_BY,FOLLOW_K_BY_in_cfamProperty2415); 
                    match(input,136,FOLLOW_136_in_cfamProperty2417); 
                    pushFollow(FOLLOW_cfamOrdering_in_cfamProperty2419);
                    cfamOrdering(expr);

                    state._fsp--;

                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:493:56: ( ',' cfamOrdering[expr] )*
                    loop51:
                    do {
                        int alt51=2;
                        int LA51_0 = input.LA(1);

                        if ( (LA51_0==138) ) {
                            alt51=1;
                        }


                        switch (alt51) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:493:57: ',' cfamOrdering[expr]
                    	    {
                    	    match(input,138,FOLLOW_138_in_cfamProperty2423); 
                    	    pushFollow(FOLLOW_cfamOrdering_in_cfamProperty2425);
                    	    cfamOrdering(expr);

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop51;
                        }
                    } while (true);

                    match(input,137,FOLLOW_137_in_cfamProperty2430); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfamProperty"


    // $ANTLR start "cfamOrdering"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:496:1: cfamOrdering[CreateTableStatement.RawStatement expr] : k= cident ( K_ASC | K_DESC ) ;
    public final void cfamOrdering(CreateTableStatement.RawStatement expr) throws RecognitionException {
        ColumnIdentifier k = null;


         boolean reversed=false; 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:5: (k= cident ( K_ASC | K_DESC ) )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:7: k= cident ( K_ASC | K_DESC )
            {
            pushFollow(FOLLOW_cident_in_cfamOrdering2458);
            k=cident();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:16: ( K_ASC | K_DESC )
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0==K_ASC) ) {
                alt53=1;
            }
            else if ( (LA53_0==K_DESC) ) {
                alt53=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;
            }
            switch (alt53) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:17: K_ASC
                    {
                    match(input,K_ASC,FOLLOW_K_ASC_in_cfamOrdering2461); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:25: K_DESC
                    {
                    match(input,K_DESC,FOLLOW_K_DESC_in_cfamOrdering2465); 
                     reversed=true;

                    }
                    break;

            }

             expr.setOrdering(k, reversed); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfamOrdering"


    // $ANTLR start "createIndexStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:501:1: createIndexStatement returns [CreateIndexStatement expr] : K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' ( K_USING cls= STRING_LITERAL )? ;
    public final CreateIndexStatement createIndexStatement() throws RecognitionException {
        CreateIndexStatement expr = null;

        Token idxName=null;
        Token cls=null;
        CFName cf = null;

        ColumnIdentifier id = null;



                boolean isCustom = false;
                boolean ifNotExists = false;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:5: ( K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' ( K_USING cls= STRING_LITERAL )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:7: K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? (idxName= IDENT )? K_ON cf= columnFamilyName '(' id= cident ')' ( K_USING cls= STRING_LITERAL )?
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createIndexStatement2503); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:16: ( K_CUSTOM )?
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0==K_CUSTOM) ) {
                alt54=1;
            }
            switch (alt54) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:17: K_CUSTOM
                    {
                    match(input,K_CUSTOM,FOLLOW_K_CUSTOM_in_createIndexStatement2506); 
                     isCustom = true; 

                    }
                    break;

            }

            match(input,K_INDEX,FOLLOW_K_INDEX_in_createIndexStatement2512); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:57: ( K_IF K_NOT K_EXISTS )?
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==K_IF) ) {
                alt55=1;
            }
            switch (alt55) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:510:58: K_IF K_NOT K_EXISTS
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_createIndexStatement2515); 
                    match(input,K_NOT,FOLLOW_K_NOT_in_createIndexStatement2517); 
                    match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createIndexStatement2519); 
                     ifNotExists = true; 

                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:511:9: (idxName= IDENT )?
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==IDENT) ) {
                alt56=1;
            }
            switch (alt56) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:511:10: idxName= IDENT
                    {
                    idxName=(Token)match(input,IDENT,FOLLOW_IDENT_in_createIndexStatement2537); 

                    }
                    break;

            }

            match(input,K_ON,FOLLOW_K_ON_in_createIndexStatement2541); 
            pushFollow(FOLLOW_columnFamilyName_in_createIndexStatement2545);
            cf=columnFamilyName();

            state._fsp--;

            match(input,136,FOLLOW_136_in_createIndexStatement2547); 
            pushFollow(FOLLOW_cident_in_createIndexStatement2551);
            id=cident();

            state._fsp--;

            match(input,137,FOLLOW_137_in_createIndexStatement2553); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:512:9: ( K_USING cls= STRING_LITERAL )?
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==K_USING) ) {
                alt57=1;
            }
            switch (alt57) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:512:11: K_USING cls= STRING_LITERAL
                    {
                    match(input,K_USING,FOLLOW_K_USING_in_createIndexStatement2565); 
                    cls=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createIndexStatement2569); 

                    }
                    break;

            }

             expr = new CreateIndexStatement(cf, (idxName!=null?idxName.getText():null), id, ifNotExists, isCustom, (cls!=null?cls.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createIndexStatement"


    // $ANTLR start "createTriggerStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:516:1: createTriggerStatement returns [CreateTriggerStatement expr] : K_CREATE K_TRIGGER (name= IDENT ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL ;
    public final CreateTriggerStatement createTriggerStatement() throws RecognitionException {
        CreateTriggerStatement expr = null;

        Token name=null;
        Token cls=null;
        CFName cf = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:5: ( K_CREATE K_TRIGGER (name= IDENT ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:7: K_CREATE K_TRIGGER (name= IDENT ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createTriggerStatement2603); 
            match(input,K_TRIGGER,FOLLOW_K_TRIGGER_in_createTriggerStatement2605); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:26: (name= IDENT )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:520:27: name= IDENT
            {
            name=(Token)match(input,IDENT,FOLLOW_IDENT_in_createTriggerStatement2610); 

            }

            match(input,K_ON,FOLLOW_K_ON_in_createTriggerStatement2613); 
            pushFollow(FOLLOW_columnFamilyName_in_createTriggerStatement2617);
            cf=columnFamilyName();

            state._fsp--;

            match(input,K_USING,FOLLOW_K_USING_in_createTriggerStatement2619); 
            cls=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createTriggerStatement2623); 
             expr = new CreateTriggerStatement(cf, (name!=null?name.getText():null), (cls!=null?cls.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "createTriggerStatement"


    // $ANTLR start "dropTriggerStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:524:1: dropTriggerStatement returns [DropTriggerStatement expr] : K_DROP K_TRIGGER (name= IDENT ) K_ON cf= columnFamilyName ;
    public final DropTriggerStatement dropTriggerStatement() throws RecognitionException {
        DropTriggerStatement expr = null;

        Token name=null;
        CFName cf = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:528:5: ( K_DROP K_TRIGGER (name= IDENT ) K_ON cf= columnFamilyName )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:528:7: K_DROP K_TRIGGER (name= IDENT ) K_ON cf= columnFamilyName
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropTriggerStatement2654); 
            match(input,K_TRIGGER,FOLLOW_K_TRIGGER_in_dropTriggerStatement2656); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:528:24: (name= IDENT )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:528:25: name= IDENT
            {
            name=(Token)match(input,IDENT,FOLLOW_IDENT_in_dropTriggerStatement2661); 

            }

            match(input,K_ON,FOLLOW_K_ON_in_dropTriggerStatement2664); 
            pushFollow(FOLLOW_columnFamilyName_in_dropTriggerStatement2668);
            cf=columnFamilyName();

            state._fsp--;

             expr = new DropTriggerStatement(cf, (name!=null?name.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "dropTriggerStatement"


    // $ANTLR start "alterKeyspaceStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:532:1: alterKeyspaceStatement returns [AlterKeyspaceStatement expr] : K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs] ;
    public final AlterKeyspaceStatement alterKeyspaceStatement() throws RecognitionException {
        AlterKeyspaceStatement expr = null;

        String ks = null;


         KSPropDefs attrs = new KSPropDefs(); 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:537:5: ( K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs] )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:537:7: K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs]
            {
            match(input,K_ALTER,FOLLOW_K_ALTER_in_alterKeyspaceStatement2708); 
            match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_alterKeyspaceStatement2710); 
            pushFollow(FOLLOW_keyspaceName_in_alterKeyspaceStatement2714);
            ks=keyspaceName();

            state._fsp--;

            match(input,K_WITH,FOLLOW_K_WITH_in_alterKeyspaceStatement2724); 
            pushFollow(FOLLOW_properties_in_alterKeyspaceStatement2726);
            properties(attrs);

            state._fsp--;

             expr = new AlterKeyspaceStatement(ks, attrs); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "alterKeyspaceStatement"


    // $ANTLR start "alterTableStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:542:1: alterTableStatement returns [AlterTableStatement expr] : K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* ) ;
    public final AlterTableStatement alterTableStatement() throws RecognitionException {
        AlterTableStatement expr = null;

        CFName cf = null;

        ColumnIdentifier id = null;

        CQL3Type v = null;

        ColumnIdentifier id1 = null;

        ColumnIdentifier toId1 = null;

        ColumnIdentifier idn = null;

        ColumnIdentifier toIdn = null;



                AlterTableStatement.Type type = null;
                CFPropDefs props = new CFPropDefs();
                Map<ColumnIdentifier, ColumnIdentifier> renames = new HashMap<ColumnIdentifier, ColumnIdentifier>();
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:555:5: ( K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* ) )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:555:7: K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* )
            {
            match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement2762); 
            match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_alterTableStatement2764); 
            pushFollow(FOLLOW_columnFamilyName_in_alterTableStatement2768);
            cf=columnFamilyName();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:556:11: ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* )
            int alt59=5;
            switch ( input.LA(1) ) {
            case K_ALTER:
                {
                alt59=1;
                }
                break;
            case K_ADD:
                {
                alt59=2;
                }
                break;
            case K_DROP:
                {
                alt59=3;
                }
                break;
            case K_WITH:
                {
                alt59=4;
                }
                break;
            case K_RENAME:
                {
                alt59=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;
            }

            switch (alt59) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:556:13: K_ALTER id= cident K_TYPE v= comparatorType
                    {
                    match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement2782); 
                    pushFollow(FOLLOW_cident_in_alterTableStatement2786);
                    id=cident();

                    state._fsp--;

                    match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTableStatement2788); 
                    pushFollow(FOLLOW_comparatorType_in_alterTableStatement2792);
                    v=comparatorType();

                    state._fsp--;

                     type = AlterTableStatement.Type.ALTER; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:557:13: K_ADD id= cident v= comparatorType
                    {
                    match(input,K_ADD,FOLLOW_K_ADD_in_alterTableStatement2808); 
                    pushFollow(FOLLOW_cident_in_alterTableStatement2814);
                    id=cident();

                    state._fsp--;

                    pushFollow(FOLLOW_comparatorType_in_alterTableStatement2818);
                    v=comparatorType();

                    state._fsp--;

                     type = AlterTableStatement.Type.ADD; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:558:13: K_DROP id= cident
                    {
                    match(input,K_DROP,FOLLOW_K_DROP_in_alterTableStatement2841); 
                    pushFollow(FOLLOW_cident_in_alterTableStatement2846);
                    id=cident();

                    state._fsp--;

                     type = AlterTableStatement.Type.DROP; 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:559:13: K_WITH properties[props]
                    {
                    match(input,K_WITH,FOLLOW_K_WITH_in_alterTableStatement2886); 
                    pushFollow(FOLLOW_properties_in_alterTableStatement2889);
                    properties(props);

                    state._fsp--;

                     type = AlterTableStatement.Type.OPTS; 

                    }
                    break;
                case 5 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:13: K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )*
                    {
                    match(input,K_RENAME,FOLLOW_K_RENAME_in_alterTableStatement2922); 
                     type = AlterTableStatement.Type.RENAME; 
                    pushFollow(FOLLOW_cident_in_alterTableStatement2976);
                    id1=cident();

                    state._fsp--;

                    match(input,K_TO,FOLLOW_K_TO_in_alterTableStatement2978); 
                    pushFollow(FOLLOW_cident_in_alterTableStatement2982);
                    toId1=cident();

                    state._fsp--;

                     renames.put(id1, toId1); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:562:16: ( K_AND idn= cident K_TO toIdn= cident )*
                    loop58:
                    do {
                        int alt58=2;
                        int LA58_0 = input.LA(1);

                        if ( (LA58_0==K_AND) ) {
                            alt58=1;
                        }


                        switch (alt58) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:562:18: K_AND idn= cident K_TO toIdn= cident
                    	    {
                    	    match(input,K_AND,FOLLOW_K_AND_in_alterTableStatement3003); 
                    	    pushFollow(FOLLOW_cident_in_alterTableStatement3007);
                    	    idn=cident();

                    	    state._fsp--;

                    	    match(input,K_TO,FOLLOW_K_TO_in_alterTableStatement3009); 
                    	    pushFollow(FOLLOW_cident_in_alterTableStatement3013);
                    	    toIdn=cident();

                    	    state._fsp--;

                    	     renames.put(idn, toIdn); 

                    	    }
                    	    break;

                    	default :
                    	    break loop58;
                        }
                    } while (true);


                    }
                    break;

            }


                    expr = new AlterTableStatement(cf, type, id, v, props, renames);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "alterTableStatement"


    // $ANTLR start "dropKeyspaceStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:569:1: dropKeyspaceStatement returns [DropKeyspaceStatement ksp] : K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName ;
    public final DropKeyspaceStatement dropKeyspaceStatement() throws RecognitionException {
        DropKeyspaceStatement ksp = null;

        String ks = null;


         boolean ifExists = false; 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:574:5: ( K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:574:7: K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropKeyspaceStatement3068); 
            match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement3070); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:574:25: ( K_IF K_EXISTS )?
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==K_IF) ) {
                alt60=1;
            }
            switch (alt60) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:574:26: K_IF K_EXISTS
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_dropKeyspaceStatement3073); 
                    match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropKeyspaceStatement3075); 
                     ifExists = true; 

                    }
                    break;

            }

            pushFollow(FOLLOW_keyspaceName_in_dropKeyspaceStatement3084);
            ks=keyspaceName();

            state._fsp--;

             ksp = new DropKeyspaceStatement(ks, ifExists); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ksp;
    }
    // $ANTLR end "dropKeyspaceStatement"


    // $ANTLR start "dropTableStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:577:1: dropTableStatement returns [DropTableStatement stmt] : K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName ;
    public final DropTableStatement dropTableStatement() throws RecognitionException {
        DropTableStatement stmt = null;

        CFName cf = null;


         boolean ifExists = false; 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:582:5: ( K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:582:7: K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropTableStatement3118); 
            match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_dropTableStatement3120); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:582:29: ( K_IF K_EXISTS )?
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0==K_IF) ) {
                alt61=1;
            }
            switch (alt61) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:582:30: K_IF K_EXISTS
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_dropTableStatement3123); 
                    match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropTableStatement3125); 
                     ifExists = true; 

                    }
                    break;

            }

            pushFollow(FOLLOW_columnFamilyName_in_dropTableStatement3134);
            cf=columnFamilyName();

            state._fsp--;

             stmt = new DropTableStatement(cf, ifExists); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "dropTableStatement"


    // $ANTLR start "dropIndexStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:585:1: dropIndexStatement returns [DropIndexStatement expr] : K_DROP K_INDEX ( K_IF K_EXISTS )? index= IDENT ;
    public final DropIndexStatement dropIndexStatement() throws RecognitionException {
        DropIndexStatement expr = null;

        Token index=null;

         boolean ifExists = false; 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:590:5: ( K_DROP K_INDEX ( K_IF K_EXISTS )? index= IDENT )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:590:7: K_DROP K_INDEX ( K_IF K_EXISTS )? index= IDENT
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropIndexStatement3168); 
            match(input,K_INDEX,FOLLOW_K_INDEX_in_dropIndexStatement3170); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:590:22: ( K_IF K_EXISTS )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==K_IF) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:590:23: K_IF K_EXISTS
                    {
                    match(input,K_IF,FOLLOW_K_IF_in_dropIndexStatement3173); 
                    match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropIndexStatement3175); 
                     ifExists = true; 

                    }
                    break;

            }

            index=(Token)match(input,IDENT,FOLLOW_IDENT_in_dropIndexStatement3184); 
             expr = new DropIndexStatement((index!=null?index.getText():null), ifExists); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return expr;
    }
    // $ANTLR end "dropIndexStatement"


    // $ANTLR start "truncateStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:594:1: truncateStatement returns [TruncateStatement stmt] : K_TRUNCATE cf= columnFamilyName ;
    public final TruncateStatement truncateStatement() throws RecognitionException {
        TruncateStatement stmt = null;

        CFName cf = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:598:5: ( K_TRUNCATE cf= columnFamilyName )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:598:7: K_TRUNCATE cf= columnFamilyName
            {
            match(input,K_TRUNCATE,FOLLOW_K_TRUNCATE_in_truncateStatement3215); 
            pushFollow(FOLLOW_columnFamilyName_in_truncateStatement3219);
            cf=columnFamilyName();

            state._fsp--;

             stmt = new TruncateStatement(cf); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "truncateStatement"


    // $ANTLR start "grantStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:601:1: grantStatement returns [GrantStatement stmt] : K_GRANT permissionOrAll K_ON resource K_TO username ;
    public final GrantStatement grantStatement() throws RecognitionException {
        GrantStatement stmt = null;

        Set<Permission> permissionOrAll1 = null;

        IResource resource2 = null;

        CqlParser.username_return username3 = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:605:5: ( K_GRANT permissionOrAll K_ON resource K_TO username )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:605:7: K_GRANT permissionOrAll K_ON resource K_TO username
            {
            match(input,K_GRANT,FOLLOW_K_GRANT_in_grantStatement3244); 
            pushFollow(FOLLOW_permissionOrAll_in_grantStatement3256);
            permissionOrAll1=permissionOrAll();

            state._fsp--;

            match(input,K_ON,FOLLOW_K_ON_in_grantStatement3264); 
            pushFollow(FOLLOW_resource_in_grantStatement3276);
            resource2=resource();

            state._fsp--;

            match(input,K_TO,FOLLOW_K_TO_in_grantStatement3284); 
            pushFollow(FOLLOW_username_in_grantStatement3296);
            username3=username();

            state._fsp--;

             stmt = new GrantStatement(permissionOrAll1, resource2, (username3!=null?input.toString(username3.start,username3.stop):null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "grantStatement"


    // $ANTLR start "revokeStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:614:1: revokeStatement returns [RevokeStatement stmt] : K_REVOKE permissionOrAll K_ON resource K_FROM username ;
    public final RevokeStatement revokeStatement() throws RecognitionException {
        RevokeStatement stmt = null;

        Set<Permission> permissionOrAll4 = null;

        IResource resource5 = null;

        CqlParser.username_return username6 = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:618:5: ( K_REVOKE permissionOrAll K_ON resource K_FROM username )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:618:7: K_REVOKE permissionOrAll K_ON resource K_FROM username
            {
            match(input,K_REVOKE,FOLLOW_K_REVOKE_in_revokeStatement3327); 
            pushFollow(FOLLOW_permissionOrAll_in_revokeStatement3339);
            permissionOrAll4=permissionOrAll();

            state._fsp--;

            match(input,K_ON,FOLLOW_K_ON_in_revokeStatement3347); 
            pushFollow(FOLLOW_resource_in_revokeStatement3359);
            resource5=resource();

            state._fsp--;

            match(input,K_FROM,FOLLOW_K_FROM_in_revokeStatement3367); 
            pushFollow(FOLLOW_username_in_revokeStatement3379);
            username6=username();

            state._fsp--;

             stmt = new RevokeStatement(permissionOrAll4, resource5, (username6!=null?input.toString(username6.start,username6.stop):null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "revokeStatement"


    // $ANTLR start "listPermissionsStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:627:1: listPermissionsStatement returns [ListPermissionsStatement stmt] : K_LIST permissionOrAll ( K_ON resource )? ( K_OF username )? ( K_NORECURSIVE )? ;
    public final ListPermissionsStatement listPermissionsStatement() throws RecognitionException {
        ListPermissionsStatement stmt = null;

        IResource resource7 = null;

        CqlParser.username_return username8 = null;

        Set<Permission> permissionOrAll9 = null;



                IResource resource = null;
                String username = null;
                boolean recursive = true;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:633:5: ( K_LIST permissionOrAll ( K_ON resource )? ( K_OF username )? ( K_NORECURSIVE )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:633:7: K_LIST permissionOrAll ( K_ON resource )? ( K_OF username )? ( K_NORECURSIVE )?
            {
            match(input,K_LIST,FOLLOW_K_LIST_in_listPermissionsStatement3417); 
            pushFollow(FOLLOW_permissionOrAll_in_listPermissionsStatement3429);
            permissionOrAll9=permissionOrAll();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:7: ( K_ON resource )?
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==K_ON) ) {
                alt63=1;
            }
            switch (alt63) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:9: K_ON resource
                    {
                    match(input,K_ON,FOLLOW_K_ON_in_listPermissionsStatement3439); 
                    pushFollow(FOLLOW_resource_in_listPermissionsStatement3441);
                    resource7=resource();

                    state._fsp--;

                     resource = resource7; 

                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:636:7: ( K_OF username )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==K_OF) ) {
                alt64=1;
            }
            switch (alt64) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:636:9: K_OF username
                    {
                    match(input,K_OF,FOLLOW_K_OF_in_listPermissionsStatement3456); 
                    pushFollow(FOLLOW_username_in_listPermissionsStatement3458);
                    username8=username();

                    state._fsp--;

                     username = (username8!=null?input.toString(username8.start,username8.stop):null); 

                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:637:7: ( K_NORECURSIVE )?
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( (LA65_0==K_NORECURSIVE) ) {
                alt65=1;
            }
            switch (alt65) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:637:9: K_NORECURSIVE
                    {
                    match(input,K_NORECURSIVE,FOLLOW_K_NORECURSIVE_in_listPermissionsStatement3473); 
                     recursive = false; 

                    }
                    break;

            }

             stmt = new ListPermissionsStatement(permissionOrAll9, resource, username, recursive); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "listPermissionsStatement"


    // $ANTLR start "permission"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:641:1: permission returns [Permission perm] : p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE ) ;
    public final Permission permission() throws RecognitionException {
        Permission perm = null;

        Token p=null;

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:642:5: (p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE ) )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:642:7: p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE )
            {
            p=(Token)input.LT(1);
            if ( input.LA(1)==K_SELECT||input.LA(1)==K_CREATE||(input.LA(1)>=K_DROP && input.LA(1)<=K_ALTER)||(input.LA(1)>=K_MODIFY && input.LA(1)<=K_AUTHORIZE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

             perm = Permission.valueOf((p!=null?p.getText():null).toUpperCase()); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return perm;
    }
    // $ANTLR end "permission"


    // $ANTLR start "permissionOrAll"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:646:1: permissionOrAll returns [Set<Permission> perms] : ( K_ALL ( K_PERMISSIONS )? | p= permission ( K_PERMISSION )? );
    public final Set<Permission> permissionOrAll() throws RecognitionException {
        Set<Permission> perms = null;

        Permission p = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:647:5: ( K_ALL ( K_PERMISSIONS )? | p= permission ( K_PERMISSION )? )
            int alt68=2;
            int LA68_0 = input.LA(1);

            if ( (LA68_0==K_ALL) ) {
                alt68=1;
            }
            else if ( (LA68_0==K_SELECT||LA68_0==K_CREATE||(LA68_0>=K_DROP && LA68_0<=K_ALTER)||(LA68_0>=K_MODIFY && LA68_0<=K_AUTHORIZE)) ) {
                alt68=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 68, 0, input);

                throw nvae;
            }
            switch (alt68) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:647:7: K_ALL ( K_PERMISSIONS )?
                    {
                    match(input,K_ALL,FOLLOW_K_ALL_in_permissionOrAll3558); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:647:13: ( K_PERMISSIONS )?
                    int alt66=2;
                    int LA66_0 = input.LA(1);

                    if ( (LA66_0==K_PERMISSIONS) ) {
                        alt66=1;
                    }
                    switch (alt66) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:647:15: K_PERMISSIONS
                            {
                            match(input,K_PERMISSIONS,FOLLOW_K_PERMISSIONS_in_permissionOrAll3562); 

                            }
                            break;

                    }

                     perms = Permission.ALL_DATA; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:648:7: p= permission ( K_PERMISSION )?
                    {
                    pushFollow(FOLLOW_permission_in_permissionOrAll3583);
                    p=permission();

                    state._fsp--;

                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:648:20: ( K_PERMISSION )?
                    int alt67=2;
                    int LA67_0 = input.LA(1);

                    if ( (LA67_0==K_PERMISSION) ) {
                        alt67=1;
                    }
                    switch (alt67) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:648:22: K_PERMISSION
                            {
                            match(input,K_PERMISSION,FOLLOW_K_PERMISSION_in_permissionOrAll3587); 

                            }
                            break;

                    }

                     perms = EnumSet.of(p); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return perms;
    }
    // $ANTLR end "permissionOrAll"


    // $ANTLR start "resource"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:651:1: resource returns [IResource res] : r= dataResource ;
    public final IResource resource() throws RecognitionException {
        IResource res = null;

        DataResource r = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:652:5: (r= dataResource )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:652:7: r= dataResource
            {
            pushFollow(FOLLOW_dataResource_in_resource3615);
            r=dataResource();

            state._fsp--;

             res = r; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return res;
    }
    // $ANTLR end "resource"


    // $ANTLR start "dataResource"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:655:1: dataResource returns [DataResource res] : ( K_ALL K_KEYSPACES | K_KEYSPACE ks= keyspaceName | ( K_COLUMNFAMILY )? cf= columnFamilyName );
    public final DataResource dataResource() throws RecognitionException {
        DataResource res = null;

        String ks = null;

        CFName cf = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:656:5: ( K_ALL K_KEYSPACES | K_KEYSPACE ks= keyspaceName | ( K_COLUMNFAMILY )? cf= columnFamilyName )
            int alt70=3;
            switch ( input.LA(1) ) {
            case K_ALL:
                {
                int LA70_1 = input.LA(2);

                if ( (LA70_1==K_KEYSPACES) ) {
                    alt70=1;
                }
                else if ( (LA70_1==EOF||LA70_1==K_FROM||LA70_1==K_TO||(LA70_1>=K_OF && LA70_1<=K_NORECURSIVE)||LA70_1==135||LA70_1==142) ) {
                    alt70=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 70, 1, input);

                    throw nvae;
                }
                }
                break;
            case K_KEYSPACE:
                {
                alt70=2;
                }
                break;
            case K_DISTINCT:
            case K_COUNT:
            case K_AS:
            case K_FILTERING:
            case K_WRITETIME:
            case K_TTL:
            case K_VALUES:
            case K_EXISTS:
            case K_TIMESTAMP:
            case K_COUNTER:
            case K_COLUMNFAMILY:
            case K_KEY:
            case K_COMPACT:
            case K_STORAGE:
            case K_CLUSTERING:
            case K_CUSTOM:
            case IDENT:
            case K_TRIGGER:
            case K_TYPE:
            case K_LIST:
            case K_PERMISSIONS:
            case K_PERMISSION:
            case K_KEYSPACES:
            case K_USER:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_USERS:
            case K_PASSWORD:
            case QUOTED_NAME:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
                {
                alt70=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 70, 0, input);

                throw nvae;
            }

            switch (alt70) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:656:7: K_ALL K_KEYSPACES
                    {
                    match(input,K_ALL,FOLLOW_K_ALL_in_dataResource3638); 
                    match(input,K_KEYSPACES,FOLLOW_K_KEYSPACES_in_dataResource3640); 
                     res = DataResource.root(); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:657:7: K_KEYSPACE ks= keyspaceName
                    {
                    match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_dataResource3650); 
                    pushFollow(FOLLOW_keyspaceName_in_dataResource3656);
                    ks=keyspaceName();

                    state._fsp--;

                     res = DataResource.keyspace(ks); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:658:7: ( K_COLUMNFAMILY )? cf= columnFamilyName
                    {
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:658:7: ( K_COLUMNFAMILY )?
                    int alt69=2;
                    int LA69_0 = input.LA(1);

                    if ( (LA69_0==K_COLUMNFAMILY) ) {
                        alt69=1;
                    }
                    switch (alt69) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:658:9: K_COLUMNFAMILY
                            {
                            match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_dataResource3668); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_columnFamilyName_in_dataResource3677);
                    cf=columnFamilyName();

                    state._fsp--;

                     res = DataResource.columnFamily(cf.getKeyspace(), cf.getColumnFamily()); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return res;
    }
    // $ANTLR end "dataResource"


    // $ANTLR start "createUserStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:662:1: createUserStatement returns [CreateUserStatement stmt] : K_CREATE K_USER username ( K_WITH userOptions[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? ;
    public final CreateUserStatement createUserStatement() throws RecognitionException {
        CreateUserStatement stmt = null;

        CqlParser.username_return username10 = null;



                UserOptions opts = new UserOptions();
                boolean superuser = false;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:670:5: ( K_CREATE K_USER username ( K_WITH userOptions[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:670:7: K_CREATE K_USER username ( K_WITH userOptions[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )?
            {
            match(input,K_CREATE,FOLLOW_K_CREATE_in_createUserStatement3717); 
            match(input,K_USER,FOLLOW_K_USER_in_createUserStatement3719); 
            pushFollow(FOLLOW_username_in_createUserStatement3721);
            username10=username();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:671:7: ( K_WITH userOptions[opts] )?
            int alt71=2;
            int LA71_0 = input.LA(1);

            if ( (LA71_0==K_WITH) ) {
                alt71=1;
            }
            switch (alt71) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:671:9: K_WITH userOptions[opts]
                    {
                    match(input,K_WITH,FOLLOW_K_WITH_in_createUserStatement3731); 
                    pushFollow(FOLLOW_userOptions_in_createUserStatement3733);
                    userOptions(opts);

                    state._fsp--;


                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:672:7: ( K_SUPERUSER | K_NOSUPERUSER )?
            int alt72=3;
            int LA72_0 = input.LA(1);

            if ( (LA72_0==K_SUPERUSER) ) {
                alt72=1;
            }
            else if ( (LA72_0==K_NOSUPERUSER) ) {
                alt72=2;
            }
            switch (alt72) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:672:9: K_SUPERUSER
                    {
                    match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_createUserStatement3747); 
                     superuser = true; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:672:45: K_NOSUPERUSER
                    {
                    match(input,K_NOSUPERUSER,FOLLOW_K_NOSUPERUSER_in_createUserStatement3753); 
                     superuser = false; 

                    }
                    break;

            }

             stmt = new CreateUserStatement((username10!=null?input.toString(username10.start,username10.stop):null), opts, superuser); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "createUserStatement"


    // $ANTLR start "alterUserStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:676:1: alterUserStatement returns [AlterUserStatement stmt] : K_ALTER K_USER username ( K_WITH userOptions[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? ;
    public final AlterUserStatement alterUserStatement() throws RecognitionException {
        AlterUserStatement stmt = null;

        CqlParser.username_return username11 = null;



                UserOptions opts = new UserOptions();
                Boolean superuser = null;
            
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:684:5: ( K_ALTER K_USER username ( K_WITH userOptions[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:684:7: K_ALTER K_USER username ( K_WITH userOptions[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )?
            {
            match(input,K_ALTER,FOLLOW_K_ALTER_in_alterUserStatement3798); 
            match(input,K_USER,FOLLOW_K_USER_in_alterUserStatement3800); 
            pushFollow(FOLLOW_username_in_alterUserStatement3802);
            username11=username();

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:685:7: ( K_WITH userOptions[opts] )?
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==K_WITH) ) {
                alt73=1;
            }
            switch (alt73) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:685:9: K_WITH userOptions[opts]
                    {
                    match(input,K_WITH,FOLLOW_K_WITH_in_alterUserStatement3812); 
                    pushFollow(FOLLOW_userOptions_in_alterUserStatement3814);
                    userOptions(opts);

                    state._fsp--;


                    }
                    break;

            }

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:686:7: ( K_SUPERUSER | K_NOSUPERUSER )?
            int alt74=3;
            int LA74_0 = input.LA(1);

            if ( (LA74_0==K_SUPERUSER) ) {
                alt74=1;
            }
            else if ( (LA74_0==K_NOSUPERUSER) ) {
                alt74=2;
            }
            switch (alt74) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:686:9: K_SUPERUSER
                    {
                    match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_alterUserStatement3828); 
                     superuser = true; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:686:45: K_NOSUPERUSER
                    {
                    match(input,K_NOSUPERUSER,FOLLOW_K_NOSUPERUSER_in_alterUserStatement3834); 
                     superuser = false; 

                    }
                    break;

            }

             stmt = new AlterUserStatement((username11!=null?input.toString(username11.start,username11.stop):null), opts, superuser); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "alterUserStatement"


    // $ANTLR start "dropUserStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:690:1: dropUserStatement returns [DropUserStatement stmt] : K_DROP K_USER username ;
    public final DropUserStatement dropUserStatement() throws RecognitionException {
        DropUserStatement stmt = null;

        CqlParser.username_return username12 = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:694:5: ( K_DROP K_USER username )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:694:7: K_DROP K_USER username
            {
            match(input,K_DROP,FOLLOW_K_DROP_in_dropUserStatement3870); 
            match(input,K_USER,FOLLOW_K_USER_in_dropUserStatement3872); 
            pushFollow(FOLLOW_username_in_dropUserStatement3874);
            username12=username();

            state._fsp--;

             stmt = new DropUserStatement((username12!=null?input.toString(username12.start,username12.stop):null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "dropUserStatement"


    // $ANTLR start "listUsersStatement"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:697:1: listUsersStatement returns [ListUsersStatement stmt] : K_LIST K_USERS ;
    public final ListUsersStatement listUsersStatement() throws RecognitionException {
        ListUsersStatement stmt = null;

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:701:5: ( K_LIST K_USERS )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:701:7: K_LIST K_USERS
            {
            match(input,K_LIST,FOLLOW_K_LIST_in_listUsersStatement3899); 
            match(input,K_USERS,FOLLOW_K_USERS_in_listUsersStatement3901); 
             stmt = new ListUsersStatement(); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stmt;
    }
    // $ANTLR end "listUsersStatement"


    // $ANTLR start "userOptions"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:704:1: userOptions[UserOptions opts] : userOption[opts] ;
    public final void userOptions(UserOptions opts) throws RecognitionException {
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:705:5: ( userOption[opts] )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:705:7: userOption[opts]
            {
            pushFollow(FOLLOW_userOption_in_userOptions3921);
            userOption(opts);

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "userOptions"


    // $ANTLR start "userOption"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:708:1: userOption[UserOptions opts] : k= K_PASSWORD v= STRING_LITERAL ;
    public final void userOption(UserOptions opts) throws RecognitionException {
        Token k=null;
        Token v=null;

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:709:5: (k= K_PASSWORD v= STRING_LITERAL )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:709:7: k= K_PASSWORD v= STRING_LITERAL
            {
            k=(Token)match(input,K_PASSWORD,FOLLOW_K_PASSWORD_in_userOption3942); 
            v=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_userOption3946); 
             opts.put((k!=null?k.getText():null), (v!=null?v.getText():null)); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "userOption"


    // $ANTLR start "cident"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:712:1: cident returns [ColumnIdentifier id] : (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword );
    public final ColumnIdentifier cident() throws RecognitionException {
        ColumnIdentifier id = null;

        Token t=null;
        String k = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:716:5: (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword )
            int alt75=3;
            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt75=1;
                }
                break;
            case QUOTED_NAME:
                {
                alt75=2;
                }
                break;
            case K_DISTINCT:
            case K_COUNT:
            case K_AS:
            case K_FILTERING:
            case K_WRITETIME:
            case K_TTL:
            case K_VALUES:
            case K_EXISTS:
            case K_TIMESTAMP:
            case K_COUNTER:
            case K_KEY:
            case K_COMPACT:
            case K_STORAGE:
            case K_CLUSTERING:
            case K_CUSTOM:
            case K_TRIGGER:
            case K_TYPE:
            case K_LIST:
            case K_ALL:
            case K_PERMISSIONS:
            case K_PERMISSION:
            case K_KEYSPACES:
            case K_USER:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_USERS:
            case K_PASSWORD:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
                {
                alt75=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 75, 0, input);

                throw nvae;
            }

            switch (alt75) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:716:7: t= IDENT
                    {
                    t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cident3975); 
                     id = new ColumnIdentifier((t!=null?t.getText():null), false); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:717:7: t= QUOTED_NAME
                    {
                    t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cident4000); 
                     id = new ColumnIdentifier((t!=null?t.getText():null), true); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:7: k= unreserved_keyword
                    {
                    pushFollow(FOLLOW_unreserved_keyword_in_cident4019);
                    k=unreserved_keyword();

                    state._fsp--;

                     id = new ColumnIdentifier(k, false); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return id;
    }
    // $ANTLR end "cident"


    // $ANTLR start "keyspaceName"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:722:1: keyspaceName returns [String id] : cfOrKsName[name, true] ;
    public final String keyspaceName() throws RecognitionException {
        String id = null;

         CFName name = new CFName(); 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:724:5: ( cfOrKsName[name, true] )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:724:7: cfOrKsName[name, true]
            {
            pushFollow(FOLLOW_cfOrKsName_in_keyspaceName4052);
            cfOrKsName(name, true);

            state._fsp--;

             id = name.getKeyspace(); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return id;
    }
    // $ANTLR end "keyspaceName"


    // $ANTLR start "columnFamilyName"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:727:1: columnFamilyName returns [CFName name] : ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false] ;
    public final CFName columnFamilyName() throws RecognitionException {
        CFName name = null;

         name = new CFName(); 
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:729:5: ( ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false] )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:729:7: ( cfOrKsName[name, true] '.' )? cfOrKsName[name, false]
            {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:729:7: ( cfOrKsName[name, true] '.' )?
            int alt76=2;
            alt76 = dfa76.predict(input);
            switch (alt76) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:729:8: cfOrKsName[name, true] '.'
                    {
                    pushFollow(FOLLOW_cfOrKsName_in_columnFamilyName4086);
                    cfOrKsName(name, true);

                    state._fsp--;

                    match(input,142,FOLLOW_142_in_columnFamilyName4089); 

                    }
                    break;

            }

            pushFollow(FOLLOW_cfOrKsName_in_columnFamilyName4093);
            cfOrKsName(name, false);

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return name;
    }
    // $ANTLR end "columnFamilyName"


    // $ANTLR start "cfOrKsName"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:732:1: cfOrKsName[CFName name, boolean isKs] : (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword );
    public final void cfOrKsName(CFName name, boolean isKs) throws RecognitionException {
        Token t=null;
        String k = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:733:5: (t= IDENT | t= QUOTED_NAME | k= unreserved_keyword )
            int alt77=3;
            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt77=1;
                }
                break;
            case QUOTED_NAME:
                {
                alt77=2;
                }
                break;
            case K_DISTINCT:
            case K_COUNT:
            case K_AS:
            case K_FILTERING:
            case K_WRITETIME:
            case K_TTL:
            case K_VALUES:
            case K_EXISTS:
            case K_TIMESTAMP:
            case K_COUNTER:
            case K_KEY:
            case K_COMPACT:
            case K_STORAGE:
            case K_CLUSTERING:
            case K_CUSTOM:
            case K_TRIGGER:
            case K_TYPE:
            case K_LIST:
            case K_ALL:
            case K_PERMISSIONS:
            case K_PERMISSION:
            case K_KEYSPACES:
            case K_USER:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_USERS:
            case K_PASSWORD:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
                {
                alt77=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                throw nvae;
            }

            switch (alt77) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:733:7: t= IDENT
                    {
                    t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cfOrKsName4114); 
                     if (isKs) name.setKeyspace((t!=null?t.getText():null), false); else name.setColumnFamily((t!=null?t.getText():null), false); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:734:7: t= QUOTED_NAME
                    {
                    t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cfOrKsName4139); 
                     if (isKs) name.setKeyspace((t!=null?t.getText():null), true); else name.setColumnFamily((t!=null?t.getText():null), true); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:735:7: k= unreserved_keyword
                    {
                    pushFollow(FOLLOW_unreserved_keyword_in_cfOrKsName4158);
                    k=unreserved_keyword();

                    state._fsp--;

                     if (isKs) name.setKeyspace(k, false); else name.setColumnFamily(k, false); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "cfOrKsName"


    // $ANTLR start "constant"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:738:1: constant returns [Constants.Literal constant] : (t= STRING_LITERAL | t= INTEGER | t= FLOAT | t= BOOLEAN | t= UUID | t= HEXNUMBER | ( '-' )? t= ( K_NAN | K_INFINITY ) );
    public final Constants.Literal constant() throws RecognitionException {
        Constants.Literal constant = null;

        Token t=null;

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:739:5: (t= STRING_LITERAL | t= INTEGER | t= FLOAT | t= BOOLEAN | t= UUID | t= HEXNUMBER | ( '-' )? t= ( K_NAN | K_INFINITY ) )
            int alt79=7;
            switch ( input.LA(1) ) {
            case STRING_LITERAL:
                {
                alt79=1;
                }
                break;
            case INTEGER:
                {
                alt79=2;
                }
                break;
            case FLOAT:
                {
                alt79=3;
                }
                break;
            case BOOLEAN:
                {
                alt79=4;
                }
                break;
            case UUID:
                {
                alt79=5;
                }
                break;
            case HEXNUMBER:
                {
                alt79=6;
                }
                break;
            case K_NAN:
            case K_INFINITY:
            case 143:
                {
                alt79=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 79, 0, input);

                throw nvae;
            }

            switch (alt79) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:739:7: t= STRING_LITERAL
                    {
                    t=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_constant4183); 
                     constant = Constants.Literal.string((t!=null?t.getText():null)); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:740:7: t= INTEGER
                    {
                    t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_constant4195); 
                     constant = Constants.Literal.integer((t!=null?t.getText():null)); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:741:7: t= FLOAT
                    {
                    t=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_constant4214); 
                     constant = Constants.Literal.floatingPoint((t!=null?t.getText():null)); 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:742:7: t= BOOLEAN
                    {
                    t=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_constant4235); 
                     constant = Constants.Literal.bool((t!=null?t.getText():null)); 

                    }
                    break;
                case 5 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:743:7: t= UUID
                    {
                    t=(Token)match(input,UUID,FOLLOW_UUID_in_constant4254); 
                     constant = Constants.Literal.uuid((t!=null?t.getText():null)); 

                    }
                    break;
                case 6 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:744:7: t= HEXNUMBER
                    {
                    t=(Token)match(input,HEXNUMBER,FOLLOW_HEXNUMBER_in_constant4276); 
                     constant = Constants.Literal.hex((t!=null?t.getText():null)); 

                    }
                    break;
                case 7 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:745:7: ( '-' )? t= ( K_NAN | K_INFINITY )
                    {
                     String sign=""; 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:745:27: ( '-' )?
                    int alt78=2;
                    int LA78_0 = input.LA(1);

                    if ( (LA78_0==143) ) {
                        alt78=1;
                    }
                    switch (alt78) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:745:28: '-'
                            {
                            match(input,143,FOLLOW_143_in_constant4294); 
                            sign = "-"; 

                            }
                            break;

                    }

                    t=(Token)input.LT(1);
                    if ( (input.LA(1)>=K_NAN && input.LA(1)<=K_INFINITY) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     constant = Constants.Literal.floatingPoint(sign + (t!=null?t.getText():null)); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return constant;
    }
    // $ANTLR end "constant"


    // $ANTLR start "map_literal"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:748:1: map_literal returns [Maps.Literal map] : '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}' ;
    public final Maps.Literal map_literal() throws RecognitionException {
        Maps.Literal map = null;

        Term.Raw k1 = null;

        Term.Raw v1 = null;

        Term.Raw kn = null;

        Term.Raw vn = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:749:5: ( '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}' )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:749:7: '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}'
            {
            match(input,144,FOLLOW_144_in_map_literal4332); 
             List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:11: (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )?
            int alt81=2;
            int LA81_0 = input.LA(1);

            if ( (LA81_0==K_DISTINCT||LA81_0==K_AS||LA81_0==K_FILTERING||LA81_0==INTEGER||LA81_0==K_VALUES||LA81_0==K_EXISTS||LA81_0==K_TIMESTAMP||LA81_0==K_COUNTER||(LA81_0>=K_KEY && LA81_0<=K_CUSTOM)||LA81_0==IDENT||(LA81_0>=STRING_LITERAL && LA81_0<=K_TRIGGER)||LA81_0==K_TYPE||LA81_0==K_LIST||(LA81_0>=K_ALL && LA81_0<=K_PASSWORD)||(LA81_0>=FLOAT && LA81_0<=K_TOKEN)||(LA81_0>=K_ASCII && LA81_0<=K_MAP)||LA81_0==136||LA81_0==140||(LA81_0>=143 && LA81_0<=145)) ) {
                alt81=1;
            }
            switch (alt81) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:13: k1= term ':' v1= term ( ',' kn= term ':' vn= term )*
                    {
                    pushFollow(FOLLOW_term_in_map_literal4350);
                    k1=term();

                    state._fsp--;

                    match(input,145,FOLLOW_145_in_map_literal4352); 
                    pushFollow(FOLLOW_term_in_map_literal4356);
                    v1=term();

                    state._fsp--;

                     m.add(Pair.create(k1, v1)); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:65: ( ',' kn= term ':' vn= term )*
                    loop80:
                    do {
                        int alt80=2;
                        int LA80_0 = input.LA(1);

                        if ( (LA80_0==138) ) {
                            alt80=1;
                        }


                        switch (alt80) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:67: ',' kn= term ':' vn= term
                    	    {
                    	    match(input,138,FOLLOW_138_in_map_literal4362); 
                    	    pushFollow(FOLLOW_term_in_map_literal4366);
                    	    kn=term();

                    	    state._fsp--;

                    	    match(input,145,FOLLOW_145_in_map_literal4368); 
                    	    pushFollow(FOLLOW_term_in_map_literal4372);
                    	    vn=term();

                    	    state._fsp--;

                    	     m.add(Pair.create(kn, vn)); 

                    	    }
                    	    break;

                    	default :
                    	    break loop80;
                        }
                    } while (true);


                    }
                    break;

            }

            match(input,146,FOLLOW_146_in_map_literal4388); 
             map = new Maps.Literal(m); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return map;
    }
    // $ANTLR end "map_literal"


    // $ANTLR start "set_or_map"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:754:1: set_or_map[Term.Raw t] returns [Term.Raw value] : ( ':' v= term ( ',' kn= term ':' vn= term )* | ( ',' tn= term )* );
    public final Term.Raw set_or_map(Term.Raw t) throws RecognitionException {
        Term.Raw value = null;

        Term.Raw v = null;

        Term.Raw kn = null;

        Term.Raw vn = null;

        Term.Raw tn = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:755:5: ( ':' v= term ( ',' kn= term ':' vn= term )* | ( ',' tn= term )* )
            int alt84=2;
            int LA84_0 = input.LA(1);

            if ( (LA84_0==145) ) {
                alt84=1;
            }
            else if ( (LA84_0==138||LA84_0==146) ) {
                alt84=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 84, 0, input);

                throw nvae;
            }
            switch (alt84) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:755:7: ':' v= term ( ',' kn= term ':' vn= term )*
                    {
                    match(input,145,FOLLOW_145_in_set_or_map4412); 
                    pushFollow(FOLLOW_term_in_set_or_map4416);
                    v=term();

                    state._fsp--;

                     List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); m.add(Pair.create(t, v)); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:756:11: ( ',' kn= term ':' vn= term )*
                    loop82:
                    do {
                        int alt82=2;
                        int LA82_0 = input.LA(1);

                        if ( (LA82_0==138) ) {
                            alt82=1;
                        }


                        switch (alt82) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:756:13: ',' kn= term ':' vn= term
                    	    {
                    	    match(input,138,FOLLOW_138_in_set_or_map4432); 
                    	    pushFollow(FOLLOW_term_in_set_or_map4436);
                    	    kn=term();

                    	    state._fsp--;

                    	    match(input,145,FOLLOW_145_in_set_or_map4438); 
                    	    pushFollow(FOLLOW_term_in_set_or_map4442);
                    	    vn=term();

                    	    state._fsp--;

                    	     m.add(Pair.create(kn, vn)); 

                    	    }
                    	    break;

                    	default :
                    	    break loop82;
                        }
                    } while (true);

                     value = new Maps.Literal(m); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:758:7: ( ',' tn= term )*
                    {
                     List<Term.Raw> s = new ArrayList<Term.Raw>(); s.add(t); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:759:11: ( ',' tn= term )*
                    loop83:
                    do {
                        int alt83=2;
                        int LA83_0 = input.LA(1);

                        if ( (LA83_0==138) ) {
                            alt83=1;
                        }


                        switch (alt83) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:759:13: ',' tn= term
                    	    {
                    	    match(input,138,FOLLOW_138_in_set_or_map4477); 
                    	    pushFollow(FOLLOW_term_in_set_or_map4481);
                    	    tn=term();

                    	    state._fsp--;

                    	     s.add(tn); 

                    	    }
                    	    break;

                    	default :
                    	    break loop83;
                        }
                    } while (true);

                     value = new Sets.Literal(s); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "set_or_map"


    // $ANTLR start "collection_literal"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:763:1: collection_literal returns [Term.Raw value] : ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= set_or_map[t] '}' | '{' '}' );
    public final Term.Raw collection_literal() throws RecognitionException {
        Term.Raw value = null;

        Term.Raw t1 = null;

        Term.Raw tn = null;

        Term.Raw t = null;

        Term.Raw v = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:764:5: ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= set_or_map[t] '}' | '{' '}' )
            int alt87=3;
            int LA87_0 = input.LA(1);

            if ( (LA87_0==140) ) {
                alt87=1;
            }
            else if ( (LA87_0==144) ) {
                int LA87_2 = input.LA(2);

                if ( (LA87_2==146) ) {
                    alt87=3;
                }
                else if ( (LA87_2==K_DISTINCT||LA87_2==K_AS||LA87_2==K_FILTERING||LA87_2==INTEGER||LA87_2==K_VALUES||LA87_2==K_EXISTS||LA87_2==K_TIMESTAMP||LA87_2==K_COUNTER||(LA87_2>=K_KEY && LA87_2<=K_CUSTOM)||LA87_2==IDENT||(LA87_2>=STRING_LITERAL && LA87_2<=K_TRIGGER)||LA87_2==K_TYPE||LA87_2==K_LIST||(LA87_2>=K_ALL && LA87_2<=K_PASSWORD)||(LA87_2>=FLOAT && LA87_2<=K_TOKEN)||(LA87_2>=K_ASCII && LA87_2<=K_MAP)||LA87_2==136||LA87_2==140||(LA87_2>=143 && LA87_2<=145)) ) {
                    alt87=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 87, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }
            switch (alt87) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:764:7: '[' (t1= term ( ',' tn= term )* )? ']'
                    {
                    match(input,140,FOLLOW_140_in_collection_literal4515); 
                     List<Term.Raw> l = new ArrayList<Term.Raw>(); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:765:11: (t1= term ( ',' tn= term )* )?
                    int alt86=2;
                    int LA86_0 = input.LA(1);

                    if ( (LA86_0==K_DISTINCT||LA86_0==K_AS||LA86_0==K_FILTERING||LA86_0==INTEGER||LA86_0==K_VALUES||LA86_0==K_EXISTS||LA86_0==K_TIMESTAMP||LA86_0==K_COUNTER||(LA86_0>=K_KEY && LA86_0<=K_CUSTOM)||LA86_0==IDENT||(LA86_0>=STRING_LITERAL && LA86_0<=K_TRIGGER)||LA86_0==K_TYPE||LA86_0==K_LIST||(LA86_0>=K_ALL && LA86_0<=K_PASSWORD)||(LA86_0>=FLOAT && LA86_0<=K_TOKEN)||(LA86_0>=K_ASCII && LA86_0<=K_MAP)||LA86_0==136||LA86_0==140||(LA86_0>=143 && LA86_0<=145)) ) {
                        alt86=1;
                    }
                    switch (alt86) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:765:13: t1= term ( ',' tn= term )*
                            {
                            pushFollow(FOLLOW_term_in_collection_literal4533);
                            t1=term();

                            state._fsp--;

                             l.add(t1); 
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:765:36: ( ',' tn= term )*
                            loop85:
                            do {
                                int alt85=2;
                                int LA85_0 = input.LA(1);

                                if ( (LA85_0==138) ) {
                                    alt85=1;
                                }


                                switch (alt85) {
                            	case 1 :
                            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:765:38: ',' tn= term
                            	    {
                            	    match(input,138,FOLLOW_138_in_collection_literal4539); 
                            	    pushFollow(FOLLOW_term_in_collection_literal4543);
                            	    tn=term();

                            	    state._fsp--;

                            	     l.add(tn); 

                            	    }
                            	    break;

                            	default :
                            	    break loop85;
                                }
                            } while (true);


                            }
                            break;

                    }

                    match(input,141,FOLLOW_141_in_collection_literal4559); 
                     value = new Lists.Literal(l); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:767:7: '{' t= term v= set_or_map[t] '}'
                    {
                    match(input,144,FOLLOW_144_in_collection_literal4569); 
                    pushFollow(FOLLOW_term_in_collection_literal4573);
                    t=term();

                    state._fsp--;

                    pushFollow(FOLLOW_set_or_map_in_collection_literal4577);
                    v=set_or_map(t);

                    state._fsp--;

                     value = v; 
                    match(input,146,FOLLOW_146_in_collection_literal4582); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:770:7: '{' '}'
                    {
                    match(input,144,FOLLOW_144_in_collection_literal4600); 
                    match(input,146,FOLLOW_146_in_collection_literal4602); 
                     value = new Sets.Literal(Collections.<Term.Raw>emptyList()); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "collection_literal"


    // $ANTLR start "value"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:773:1: value returns [Term.Raw value] : (c= constant | l= collection_literal | K_NULL | ':' id= cident | QMARK );
    public final Term.Raw value() throws RecognitionException {
        Term.Raw value = null;

        Constants.Literal c = null;

        Term.Raw l = null;

        ColumnIdentifier id = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:774:5: (c= constant | l= collection_literal | K_NULL | ':' id= cident | QMARK )
            int alt88=5;
            switch ( input.LA(1) ) {
            case INTEGER:
            case STRING_LITERAL:
            case FLOAT:
            case BOOLEAN:
            case UUID:
            case HEXNUMBER:
            case K_NAN:
            case K_INFINITY:
            case 143:
                {
                alt88=1;
                }
                break;
            case 140:
            case 144:
                {
                alt88=2;
                }
                break;
            case K_NULL:
                {
                alt88=3;
                }
                break;
            case 145:
                {
                alt88=4;
                }
                break;
            case QMARK:
                {
                alt88=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 88, 0, input);

                throw nvae;
            }

            switch (alt88) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:774:7: c= constant
                    {
                    pushFollow(FOLLOW_constant_in_value4627);
                    c=constant();

                    state._fsp--;

                     value = c; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:775:7: l= collection_literal
                    {
                    pushFollow(FOLLOW_collection_literal_in_value4649);
                    l=collection_literal();

                    state._fsp--;

                     value = l; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:776:7: K_NULL
                    {
                    match(input,K_NULL,FOLLOW_K_NULL_in_value4659); 
                     value = Constants.NULL_LITERAL; 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:777:7: ':' id= cident
                    {
                    match(input,145,FOLLOW_145_in_value4683); 
                    pushFollow(FOLLOW_cident_in_value4687);
                    id=cident();

                    state._fsp--;

                     value = newBindVariables(id); 

                    }
                    break;
                case 5 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:778:7: QMARK
                    {
                    match(input,QMARK,FOLLOW_QMARK_in_value4704); 
                     value = newBindVariables(null); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "value"


    // $ANTLR start "intValue"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:781:1: intValue returns [Term.Raw value] : ( | t= INTEGER | ':' id= cident | QMARK );
    public final Term.Raw intValue() throws RecognitionException {
        Term.Raw value = null;

        Token t=null;
        ColumnIdentifier id = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:782:5: ( | t= INTEGER | ':' id= cident | QMARK )
            int alt89=4;
            switch ( input.LA(1) ) {
            case EOF:
            case K_WHERE:
            case K_ALLOW:
            case K_AND:
            case K_INSERT:
            case K_UPDATE:
            case K_SET:
            case K_DELETE:
            case K_APPLY:
            case 135:
                {
                alt89=1;
                }
                break;
            case INTEGER:
                {
                alt89=2;
                }
                break;
            case 145:
                {
                alt89=3;
                }
                break;
            case QMARK:
                {
                alt89=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }

            switch (alt89) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:783:5: 
                    {
                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:783:7: t= INTEGER
                    {
                    t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_intValue4750); 
                     value = Constants.Literal.integer((t!=null?t.getText():null)); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:784:7: ':' id= cident
                    {
                    match(input,145,FOLLOW_145_in_intValue4764); 
                    pushFollow(FOLLOW_cident_in_intValue4768);
                    id=cident();

                    state._fsp--;

                     value = newBindVariables(id); 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:785:7: QMARK
                    {
                    match(input,QMARK,FOLLOW_QMARK_in_intValue4778); 
                     value = newBindVariables(null); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "intValue"


    // $ANTLR start "functionName"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:788:1: functionName returns [String s] : (f= IDENT | u= unreserved_function_keyword | K_TOKEN );
    public final String functionName() throws RecognitionException {
        String s = null;

        Token f=null;
        String u = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:789:5: (f= IDENT | u= unreserved_function_keyword | K_TOKEN )
            int alt90=3;
            switch ( input.LA(1) ) {
            case IDENT:
                {
                alt90=1;
                }
                break;
            case K_DISTINCT:
            case K_AS:
            case K_FILTERING:
            case K_VALUES:
            case K_EXISTS:
            case K_TIMESTAMP:
            case K_COUNTER:
            case K_KEY:
            case K_COMPACT:
            case K_STORAGE:
            case K_CLUSTERING:
            case K_CUSTOM:
            case K_TRIGGER:
            case K_TYPE:
            case K_LIST:
            case K_ALL:
            case K_PERMISSIONS:
            case K_PERMISSION:
            case K_KEYSPACES:
            case K_USER:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_USERS:
            case K_PASSWORD:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
                {
                alt90=2;
                }
                break;
            case K_TOKEN:
                {
                alt90=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 90, 0, input);

                throw nvae;
            }

            switch (alt90) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:789:7: f= IDENT
                    {
                    f=(Token)match(input,IDENT,FOLLOW_IDENT_in_functionName4811); 
                     s = (f!=null?f.getText():null); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:790:7: u= unreserved_function_keyword
                    {
                    pushFollow(FOLLOW_unreserved_function_keyword_in_functionName4845);
                    u=unreserved_function_keyword();

                    state._fsp--;

                     s = u; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:791:7: K_TOKEN
                    {
                    match(input,K_TOKEN,FOLLOW_K_TOKEN_in_functionName4855); 
                     s = "token"; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "functionName"


    // $ANTLR start "functionArgs"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:794:1: functionArgs returns [List<Term.Raw> a] : ( '(' ')' | '(' t1= term ( ',' tn= term )* ')' );
    public final List<Term.Raw> functionArgs() throws RecognitionException {
        List<Term.Raw> a = null;

        Term.Raw t1 = null;

        Term.Raw tn = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:795:5: ( '(' ')' | '(' t1= term ( ',' tn= term )* ')' )
            int alt92=2;
            int LA92_0 = input.LA(1);

            if ( (LA92_0==136) ) {
                int LA92_1 = input.LA(2);

                if ( (LA92_1==137) ) {
                    alt92=1;
                }
                else if ( (LA92_1==K_DISTINCT||LA92_1==K_AS||LA92_1==K_FILTERING||LA92_1==INTEGER||LA92_1==K_VALUES||LA92_1==K_EXISTS||LA92_1==K_TIMESTAMP||LA92_1==K_COUNTER||(LA92_1>=K_KEY && LA92_1<=K_CUSTOM)||LA92_1==IDENT||(LA92_1>=STRING_LITERAL && LA92_1<=K_TRIGGER)||LA92_1==K_TYPE||LA92_1==K_LIST||(LA92_1>=K_ALL && LA92_1<=K_PASSWORD)||(LA92_1>=FLOAT && LA92_1<=K_TOKEN)||(LA92_1>=K_ASCII && LA92_1<=K_MAP)||LA92_1==136||LA92_1==140||(LA92_1>=143 && LA92_1<=145)) ) {
                    alt92=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 92, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 92, 0, input);

                throw nvae;
            }
            switch (alt92) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:795:7: '(' ')'
                    {
                    match(input,136,FOLLOW_136_in_functionArgs4900); 
                    match(input,137,FOLLOW_137_in_functionArgs4902); 
                     a = Collections.emptyList(); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:796:7: '(' t1= term ( ',' tn= term )* ')'
                    {
                    match(input,136,FOLLOW_136_in_functionArgs4912); 
                    pushFollow(FOLLOW_term_in_functionArgs4916);
                    t1=term();

                    state._fsp--;

                     List<Term.Raw> args = new ArrayList<Term.Raw>(); args.add(t1); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:797:11: ( ',' tn= term )*
                    loop91:
                    do {
                        int alt91=2;
                        int LA91_0 = input.LA(1);

                        if ( (LA91_0==138) ) {
                            alt91=1;
                        }


                        switch (alt91) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:797:13: ',' tn= term
                    	    {
                    	    match(input,138,FOLLOW_138_in_functionArgs4932); 
                    	    pushFollow(FOLLOW_term_in_functionArgs4936);
                    	    tn=term();

                    	    state._fsp--;

                    	     args.add(tn); 

                    	    }
                    	    break;

                    	default :
                    	    break loop91;
                        }
                    } while (true);

                    match(input,137,FOLLOW_137_in_functionArgs4950); 
                     a = args; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return a;
    }
    // $ANTLR end "functionArgs"


    // $ANTLR start "term"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:801:1: term returns [Term.Raw term] : (v= value | f= functionName args= functionArgs | '(' c= comparatorType ')' t= term );
    public final Term.Raw term() throws RecognitionException {
        Term.Raw term = null;

        Term.Raw v = null;

        String f = null;

        List<Term.Raw> args = null;

        CQL3Type c = null;

        Term.Raw t = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:802:5: (v= value | f= functionName args= functionArgs | '(' c= comparatorType ')' t= term )
            int alt93=3;
            switch ( input.LA(1) ) {
            case INTEGER:
            case STRING_LITERAL:
            case FLOAT:
            case BOOLEAN:
            case UUID:
            case HEXNUMBER:
            case K_NAN:
            case K_INFINITY:
            case K_NULL:
            case QMARK:
            case 140:
            case 143:
            case 144:
            case 145:
                {
                alt93=1;
                }
                break;
            case K_DISTINCT:
            case K_AS:
            case K_FILTERING:
            case K_VALUES:
            case K_EXISTS:
            case K_TIMESTAMP:
            case K_COUNTER:
            case K_KEY:
            case K_COMPACT:
            case K_STORAGE:
            case K_CLUSTERING:
            case K_CUSTOM:
            case IDENT:
            case K_TRIGGER:
            case K_TYPE:
            case K_LIST:
            case K_ALL:
            case K_PERMISSIONS:
            case K_PERMISSION:
            case K_KEYSPACES:
            case K_USER:
            case K_SUPERUSER:
            case K_NOSUPERUSER:
            case K_USERS:
            case K_PASSWORD:
            case K_TOKEN:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
            case K_MAP:
                {
                alt93=2;
                }
                break;
            case 136:
                {
                alt93=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 93, 0, input);

                throw nvae;
            }

            switch (alt93) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:802:7: v= value
                    {
                    pushFollow(FOLLOW_value_in_term4975);
                    v=value();

                    state._fsp--;

                     term = v; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:803:7: f= functionName args= functionArgs
                    {
                    pushFollow(FOLLOW_functionName_in_term5012);
                    f=functionName();

                    state._fsp--;

                    pushFollow(FOLLOW_functionArgs_in_term5016);
                    args=functionArgs();

                    state._fsp--;

                     term = new FunctionCall.Raw(f, args); 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:804:7: '(' c= comparatorType ')' t= term
                    {
                    match(input,136,FOLLOW_136_in_term5026); 
                    pushFollow(FOLLOW_comparatorType_in_term5030);
                    c=comparatorType();

                    state._fsp--;

                    match(input,137,FOLLOW_137_in_term5032); 
                    pushFollow(FOLLOW_term_in_term5036);
                    t=term();

                    state._fsp--;

                     term = new TypeCast(c, t); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return term;
    }
    // $ANTLR end "term"


    // $ANTLR start "columnOperation"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:807:1: columnOperation[List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations] : (key= cident '=' t= term ( '+' c= cident )? | key= cident '=' c= cident sig= ( '+' | '-' ) t= term | key= cident '=' c= cident i= INTEGER | key= cident '[' k= term ']' '=' t= term );
    public final void columnOperation(List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations) throws RecognitionException {
        Token sig=null;
        Token i=null;
        ColumnIdentifier key = null;

        Term.Raw t = null;

        ColumnIdentifier c = null;

        Term.Raw k = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:808:5: (key= cident '=' t= term ( '+' c= cident )? | key= cident '=' c= cident sig= ( '+' | '-' ) t= term | key= cident '=' c= cident i= INTEGER | key= cident '[' k= term ']' '=' t= term )
            int alt95=4;
            alt95 = dfa95.predict(input);
            switch (alt95) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:808:7: key= cident '=' t= term ( '+' c= cident )?
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation5059);
                    key=cident();

                    state._fsp--;

                    match(input,147,FOLLOW_147_in_columnOperation5061); 
                    pushFollow(FOLLOW_term_in_columnOperation5065);
                    t=term();

                    state._fsp--;

                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:808:29: ( '+' c= cident )?
                    int alt94=2;
                    int LA94_0 = input.LA(1);

                    if ( (LA94_0==148) ) {
                        alt94=1;
                    }
                    switch (alt94) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:808:30: '+' c= cident
                            {
                            match(input,148,FOLLOW_148_in_columnOperation5068); 
                            pushFollow(FOLLOW_cident_in_columnOperation5072);
                            c=cident();

                            state._fsp--;


                            }
                            break;

                    }


                              if (c == null)
                              {
                                  addRawUpdate(operations, key, new Operation.SetValue(t));
                              }
                              else
                              {
                                  if (!key.equals(c))
                                      addRecognitionError("Only expressions of the form X = <value> + X are supported.");
                                  addRawUpdate(operations, key, new Operation.Prepend(t));
                              }
                          

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:821:7: key= cident '=' c= cident sig= ( '+' | '-' ) t= term
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation5093);
                    key=cident();

                    state._fsp--;

                    match(input,147,FOLLOW_147_in_columnOperation5095); 
                    pushFollow(FOLLOW_cident_in_columnOperation5099);
                    c=cident();

                    state._fsp--;

                    sig=(Token)input.LT(1);
                    if ( input.LA(1)==143||input.LA(1)==148 ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_term_in_columnOperation5113);
                    t=term();

                    state._fsp--;


                              if (!key.equals(c))
                                  addRecognitionError("Only expressions of the form X = X " + (sig!=null?sig.getText():null) + "<value> are supported.");
                              addRawUpdate(operations, key, (sig!=null?sig.getText():null).equals("+") ? new Operation.Addition(t) : new Operation.Substraction(t));
                          

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:827:7: key= cident '=' c= cident i= INTEGER
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation5131);
                    key=cident();

                    state._fsp--;

                    match(input,147,FOLLOW_147_in_columnOperation5133); 
                    pushFollow(FOLLOW_cident_in_columnOperation5137);
                    c=cident();

                    state._fsp--;

                    i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_columnOperation5141); 

                              // Note that this production *is* necessary because X = X - 3 will in fact be lexed as [ X, '=', X, INTEGER].
                              if (!key.equals(c))
                                  // We don't yet allow a '+' in front of an integer, but we could in the future really, so let's be future-proof in our error message
                                  addRecognitionError("Only expressions of the form X = X " + ((i!=null?i.getText():null).charAt(0) == '-' ? '-' : '+') + " <value> are supported.");
                              addRawUpdate(operations, key, new Operation.Addition(Constants.Literal.integer((i!=null?i.getText():null))));
                          

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:835:7: key= cident '[' k= term ']' '=' t= term
                    {
                    pushFollow(FOLLOW_cident_in_columnOperation5159);
                    key=cident();

                    state._fsp--;

                    match(input,140,FOLLOW_140_in_columnOperation5161); 
                    pushFollow(FOLLOW_term_in_columnOperation5165);
                    k=term();

                    state._fsp--;

                    match(input,141,FOLLOW_141_in_columnOperation5167); 
                    match(input,147,FOLLOW_147_in_columnOperation5169); 
                    pushFollow(FOLLOW_term_in_columnOperation5173);
                    t=term();

                    state._fsp--;


                              addRawUpdate(operations, key, new Operation.SetElement(k, t));
                          

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "columnOperation"


    // $ANTLR start "properties"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:841:1: properties[PropertyDefinitions props] : property[props] ( K_AND property[props] )* ;
    public final void properties(PropertyDefinitions props) throws RecognitionException {
        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:842:5: ( property[props] ( K_AND property[props] )* )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:842:7: property[props] ( K_AND property[props] )*
            {
            pushFollow(FOLLOW_property_in_properties5199);
            property(props);

            state._fsp--;

            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:842:23: ( K_AND property[props] )*
            loop96:
            do {
                int alt96=2;
                int LA96_0 = input.LA(1);

                if ( (LA96_0==K_AND) ) {
                    alt96=1;
                }


                switch (alt96) {
            	case 1 :
            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:842:24: K_AND property[props]
            	    {
            	    match(input,K_AND,FOLLOW_K_AND_in_properties5203); 
            	    pushFollow(FOLLOW_property_in_properties5205);
            	    property(props);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop96;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "properties"


    // $ANTLR start "property"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:845:1: property[PropertyDefinitions props] : k= cident '=' (simple= propertyValue | map= map_literal ) ;
    public final void property(PropertyDefinitions props) throws RecognitionException {
        ColumnIdentifier k = null;

        String simple = null;

        Maps.Literal map = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:846:5: (k= cident '=' (simple= propertyValue | map= map_literal ) )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:846:7: k= cident '=' (simple= propertyValue | map= map_literal )
            {
            pushFollow(FOLLOW_cident_in_property5228);
            k=cident();

            state._fsp--;

            match(input,147,FOLLOW_147_in_property5230); 
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:846:20: (simple= propertyValue | map= map_literal )
            int alt97=2;
            int LA97_0 = input.LA(1);

            if ( ((LA97_0>=K_DISTINCT && LA97_0<=K_AS)||(LA97_0>=K_FILTERING && LA97_0<=INTEGER)||LA97_0==K_VALUES||LA97_0==K_EXISTS||LA97_0==K_TIMESTAMP||LA97_0==K_COUNTER||(LA97_0>=K_KEY && LA97_0<=K_CUSTOM)||(LA97_0>=STRING_LITERAL && LA97_0<=K_TRIGGER)||LA97_0==K_TYPE||LA97_0==K_LIST||(LA97_0>=K_ALL && LA97_0<=K_PASSWORD)||(LA97_0>=FLOAT && LA97_0<=K_INFINITY)||(LA97_0>=K_ASCII && LA97_0<=K_MAP)||LA97_0==143) ) {
                alt97=1;
            }
            else if ( (LA97_0==144) ) {
                alt97=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 97, 0, input);

                throw nvae;
            }
            switch (alt97) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:846:21: simple= propertyValue
                    {
                    pushFollow(FOLLOW_propertyValue_in_property5235);
                    simple=propertyValue();

                    state._fsp--;

                     try { props.addProperty(k.toString(), simple); } catch (SyntaxException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:847:24: map= map_literal
                    {
                    pushFollow(FOLLOW_map_literal_in_property5264);
                    map=map_literal();

                    state._fsp--;

                     try { props.addProperty(k.toString(), convertPropertyMap(map)); } catch (SyntaxException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "propertyValue"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:850:1: propertyValue returns [String str] : (c= constant | u= unreserved_keyword );
    public final String propertyValue() throws RecognitionException {
        String str = null;

        Constants.Literal c = null;

        String u = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:851:5: (c= constant | u= unreserved_keyword )
            int alt98=2;
            int LA98_0 = input.LA(1);

            if ( (LA98_0==INTEGER||LA98_0==STRING_LITERAL||(LA98_0>=FLOAT && LA98_0<=K_INFINITY)||LA98_0==143) ) {
                alt98=1;
            }
            else if ( ((LA98_0>=K_DISTINCT && LA98_0<=K_AS)||(LA98_0>=K_FILTERING && LA98_0<=K_TTL)||LA98_0==K_VALUES||LA98_0==K_EXISTS||LA98_0==K_TIMESTAMP||LA98_0==K_COUNTER||(LA98_0>=K_KEY && LA98_0<=K_CUSTOM)||LA98_0==K_TRIGGER||LA98_0==K_TYPE||LA98_0==K_LIST||(LA98_0>=K_ALL && LA98_0<=K_PASSWORD)||(LA98_0>=K_ASCII && LA98_0<=K_MAP)) ) {
                alt98=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 98, 0, input);

                throw nvae;
            }
            switch (alt98) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:851:7: c= constant
                    {
                    pushFollow(FOLLOW_constant_in_propertyValue5292);
                    c=constant();

                    state._fsp--;

                     str = c.getRawText(); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:852:7: u= unreserved_keyword
                    {
                    pushFollow(FOLLOW_unreserved_keyword_in_propertyValue5314);
                    u=unreserved_keyword();

                    state._fsp--;

                     str = u; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "propertyValue"


    // $ANTLR start "relationType"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:855:1: relationType returns [Relation.Type op] : ( '=' | '<' | '<=' | '>' | '>=' );
    public final Relation.Type relationType() throws RecognitionException {
        Relation.Type op = null;

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:856:5: ( '=' | '<' | '<=' | '>' | '>=' )
            int alt99=5;
            switch ( input.LA(1) ) {
            case 147:
                {
                alt99=1;
                }
                break;
            case 149:
                {
                alt99=2;
                }
                break;
            case 150:
                {
                alt99=3;
                }
                break;
            case 151:
                {
                alt99=4;
                }
                break;
            case 152:
                {
                alt99=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 99, 0, input);

                throw nvae;
            }

            switch (alt99) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:856:7: '='
                    {
                    match(input,147,FOLLOW_147_in_relationType5337); 
                     op = Relation.Type.EQ; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:857:7: '<'
                    {
                    match(input,149,FOLLOW_149_in_relationType5348); 
                     op = Relation.Type.LT; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:858:7: '<='
                    {
                    match(input,150,FOLLOW_150_in_relationType5359); 
                     op = Relation.Type.LTE; 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:859:7: '>'
                    {
                    match(input,151,FOLLOW_151_in_relationType5369); 
                     op = Relation.Type.GT; 

                    }
                    break;
                case 5 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:860:7: '>='
                    {
                    match(input,152,FOLLOW_152_in_relationType5380); 
                     op = Relation.Type.GTE; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return op;
    }
    // $ANTLR end "relationType"


    // $ANTLR start "relation"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:863:1: relation[List<Relation> clauses] : (name= cident type= relationType t= term | K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term | name= cident K_IN ( QMARK | ':' mid= cident ) | name= cident K_IN '(' (f1= term ( ',' fN= term )* )? ')' | '(' relation[$clauses] ')' );
    public final void relation(List<Relation> clauses) throws RecognitionException {
        ColumnIdentifier name = null;

        Relation.Type type = null;

        Term.Raw t = null;

        ColumnIdentifier name1 = null;

        ColumnIdentifier namen = null;

        ColumnIdentifier mid = null;

        Term.Raw f1 = null;

        Term.Raw fN = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:864:5: (name= cident type= relationType t= term | K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term | name= cident K_IN ( QMARK | ':' mid= cident ) | name= cident K_IN '(' (f1= term ( ',' fN= term )* )? ')' | '(' relation[$clauses] ')' )
            int alt104=5;
            alt104 = dfa104.predict(input);
            switch (alt104) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:864:7: name= cident type= relationType t= term
                    {
                    pushFollow(FOLLOW_cident_in_relation5402);
                    name=cident();

                    state._fsp--;

                    pushFollow(FOLLOW_relationType_in_relation5406);
                    type=relationType();

                    state._fsp--;

                    pushFollow(FOLLOW_term_in_relation5410);
                    t=term();

                    state._fsp--;

                     clauses.add(new Relation(name, type, t)); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:865:7: K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term
                    {
                    match(input,K_TOKEN,FOLLOW_K_TOKEN_in_relation5420); 
                     List<ColumnIdentifier> l = new ArrayList<ColumnIdentifier>(); 
                    match(input,136,FOLLOW_136_in_relation5443); 
                    pushFollow(FOLLOW_cident_in_relation5447);
                    name1=cident();

                    state._fsp--;

                     l.add(name1); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:867:46: ( ',' namen= cident )*
                    loop100:
                    do {
                        int alt100=2;
                        int LA100_0 = input.LA(1);

                        if ( (LA100_0==138) ) {
                            alt100=1;
                        }


                        switch (alt100) {
                    	case 1 :
                    	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:867:48: ',' namen= cident
                    	    {
                    	    match(input,138,FOLLOW_138_in_relation5453); 
                    	    pushFollow(FOLLOW_cident_in_relation5457);
                    	    namen=cident();

                    	    state._fsp--;

                    	     l.add(namen); 

                    	    }
                    	    break;

                    	default :
                    	    break loop100;
                        }
                    } while (true);

                    match(input,137,FOLLOW_137_in_relation5463); 
                    pushFollow(FOLLOW_relationType_in_relation5475);
                    type=relationType();

                    state._fsp--;

                    pushFollow(FOLLOW_term_in_relation5479);
                    t=term();

                    state._fsp--;


                                for (ColumnIdentifier id : l)
                                    clauses.add(new Relation(id, type, t, true));
                            

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:873:7: name= cident K_IN ( QMARK | ':' mid= cident )
                    {
                    pushFollow(FOLLOW_cident_in_relation5499);
                    name=cident();

                    state._fsp--;

                    match(input,K_IN,FOLLOW_K_IN_in_relation5501); 
                     Term.Raw marker = null; 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:873:52: ( QMARK | ':' mid= cident )
                    int alt101=2;
                    int LA101_0 = input.LA(1);

                    if ( (LA101_0==QMARK) ) {
                        alt101=1;
                    }
                    else if ( (LA101_0==145) ) {
                        alt101=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 101, 0, input);

                        throw nvae;
                    }
                    switch (alt101) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:873:53: QMARK
                            {
                            match(input,QMARK,FOLLOW_QMARK_in_relation5506); 
                             marker = newINBindVariables(null); 

                            }
                            break;
                        case 2 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:873:100: ':' mid= cident
                            {
                            match(input,145,FOLLOW_145_in_relation5512); 
                            pushFollow(FOLLOW_cident_in_relation5516);
                            mid=cident();

                            state._fsp--;

                             marker = newINBindVariables(mid); 

                            }
                            break;

                    }

                     clauses.add(new Relation(name, Relation.Type.IN, marker)); 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:875:7: name= cident K_IN '(' (f1= term ( ',' fN= term )* )? ')'
                    {
                    pushFollow(FOLLOW_cident_in_relation5539);
                    name=cident();

                    state._fsp--;

                    match(input,K_IN,FOLLOW_K_IN_in_relation5541); 
                     Relation rel = Relation.createInRelation(name); 
                    match(input,136,FOLLOW_136_in_relation5552); 
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:876:12: (f1= term ( ',' fN= term )* )?
                    int alt103=2;
                    int LA103_0 = input.LA(1);

                    if ( (LA103_0==K_DISTINCT||LA103_0==K_AS||LA103_0==K_FILTERING||LA103_0==INTEGER||LA103_0==K_VALUES||LA103_0==K_EXISTS||LA103_0==K_TIMESTAMP||LA103_0==K_COUNTER||(LA103_0>=K_KEY && LA103_0<=K_CUSTOM)||LA103_0==IDENT||(LA103_0>=STRING_LITERAL && LA103_0<=K_TRIGGER)||LA103_0==K_TYPE||LA103_0==K_LIST||(LA103_0>=K_ALL && LA103_0<=K_PASSWORD)||(LA103_0>=FLOAT && LA103_0<=K_TOKEN)||(LA103_0>=K_ASCII && LA103_0<=K_MAP)||LA103_0==136||LA103_0==140||(LA103_0>=143 && LA103_0<=145)) ) {
                        alt103=1;
                    }
                    switch (alt103) {
                        case 1 :
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:876:14: f1= term ( ',' fN= term )*
                            {
                            pushFollow(FOLLOW_term_in_relation5558);
                            f1=term();

                            state._fsp--;

                             rel.addInValue(f1); 
                            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:876:46: ( ',' fN= term )*
                            loop102:
                            do {
                                int alt102=2;
                                int LA102_0 = input.LA(1);

                                if ( (LA102_0==138) ) {
                                    alt102=1;
                                }


                                switch (alt102) {
                            	case 1 :
                            	    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:876:47: ',' fN= term
                            	    {
                            	    match(input,138,FOLLOW_138_in_relation5563); 
                            	    pushFollow(FOLLOW_term_in_relation5567);
                            	    fN=term();

                            	    state._fsp--;

                            	     rel.addInValue(fN); 

                            	    }
                            	    break;

                            	default :
                            	    break loop102;
                                }
                            } while (true);


                            }
                            break;

                    }

                    match(input,137,FOLLOW_137_in_relation5577); 
                     clauses.add(rel); 

                    }
                    break;
                case 5 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:877:7: '(' relation[$clauses] ')'
                    {
                    match(input,136,FOLLOW_136_in_relation5587); 
                    pushFollow(FOLLOW_relation_in_relation5589);
                    relation(clauses);

                    state._fsp--;

                    match(input,137,FOLLOW_137_in_relation5592); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "relation"


    // $ANTLR start "comparatorType"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:880:1: comparatorType returns [CQL3Type t] : (c= native_type | c= collection_type | s= STRING_LITERAL );
    public final CQL3Type comparatorType() throws RecognitionException {
        CQL3Type t = null;

        Token s=null;
        CQL3Type c = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:881:5: (c= native_type | c= collection_type | s= STRING_LITERAL )
            int alt105=3;
            switch ( input.LA(1) ) {
            case K_TIMESTAMP:
            case K_COUNTER:
            case K_ASCII:
            case K_BIGINT:
            case K_BLOB:
            case K_BOOLEAN:
            case K_DECIMAL:
            case K_DOUBLE:
            case K_FLOAT:
            case K_INET:
            case K_INT:
            case K_TEXT:
            case K_UUID:
            case K_VARCHAR:
            case K_VARINT:
            case K_TIMEUUID:
                {
                alt105=1;
                }
                break;
            case K_SET:
            case K_LIST:
            case K_MAP:
                {
                alt105=2;
                }
                break;
            case STRING_LITERAL:
                {
                alt105=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 105, 0, input);

                throw nvae;
            }

            switch (alt105) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:881:7: c= native_type
                    {
                    pushFollow(FOLLOW_native_type_in_comparatorType5615);
                    c=native_type();

                    state._fsp--;

                     t = c; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:882:7: c= collection_type
                    {
                    pushFollow(FOLLOW_collection_type_in_comparatorType5631);
                    c=collection_type();

                    state._fsp--;

                     t = c; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:883:7: s= STRING_LITERAL
                    {
                    s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_comparatorType5643); 

                            try {
                                t = new CQL3Type.Custom((s!=null?s.getText():null));
                            } catch (SyntaxException e) {
                                addRecognitionError("Cannot parse type " + (s!=null?s.getText():null) + ": " + e.getMessage());
                            } catch (ConfigurationException e) {
                                addRecognitionError("Error setting type " + (s!=null?s.getText():null) + ": " + e.getMessage());
                            }
                          

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end "comparatorType"


    // $ANTLR start "native_type"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:895:1: native_type returns [CQL3Type t] : ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_TEXT | K_TIMESTAMP | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID );
    public final CQL3Type native_type() throws RecognitionException {
        CQL3Type t = null;

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:896:5: ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_TEXT | K_TIMESTAMP | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID )
            int alt106=16;
            switch ( input.LA(1) ) {
            case K_ASCII:
                {
                alt106=1;
                }
                break;
            case K_BIGINT:
                {
                alt106=2;
                }
                break;
            case K_BLOB:
                {
                alt106=3;
                }
                break;
            case K_BOOLEAN:
                {
                alt106=4;
                }
                break;
            case K_COUNTER:
                {
                alt106=5;
                }
                break;
            case K_DECIMAL:
                {
                alt106=6;
                }
                break;
            case K_DOUBLE:
                {
                alt106=7;
                }
                break;
            case K_FLOAT:
                {
                alt106=8;
                }
                break;
            case K_INET:
                {
                alt106=9;
                }
                break;
            case K_INT:
                {
                alt106=10;
                }
                break;
            case K_TEXT:
                {
                alt106=11;
                }
                break;
            case K_TIMESTAMP:
                {
                alt106=12;
                }
                break;
            case K_UUID:
                {
                alt106=13;
                }
                break;
            case K_VARCHAR:
                {
                alt106=14;
                }
                break;
            case K_VARINT:
                {
                alt106=15;
                }
                break;
            case K_TIMEUUID:
                {
                alt106=16;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 106, 0, input);

                throw nvae;
            }

            switch (alt106) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:896:7: K_ASCII
                    {
                    match(input,K_ASCII,FOLLOW_K_ASCII_in_native_type5672); 
                     t = CQL3Type.Native.ASCII; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:897:7: K_BIGINT
                    {
                    match(input,K_BIGINT,FOLLOW_K_BIGINT_in_native_type5686); 
                     t = CQL3Type.Native.BIGINT; 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:898:7: K_BLOB
                    {
                    match(input,K_BLOB,FOLLOW_K_BLOB_in_native_type5699); 
                     t = CQL3Type.Native.BLOB; 

                    }
                    break;
                case 4 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:899:7: K_BOOLEAN
                    {
                    match(input,K_BOOLEAN,FOLLOW_K_BOOLEAN_in_native_type5714); 
                     t = CQL3Type.Native.BOOLEAN; 

                    }
                    break;
                case 5 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:900:7: K_COUNTER
                    {
                    match(input,K_COUNTER,FOLLOW_K_COUNTER_in_native_type5726); 
                     t = CQL3Type.Native.COUNTER; 

                    }
                    break;
                case 6 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:901:7: K_DECIMAL
                    {
                    match(input,K_DECIMAL,FOLLOW_K_DECIMAL_in_native_type5738); 
                     t = CQL3Type.Native.DECIMAL; 

                    }
                    break;
                case 7 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:902:7: K_DOUBLE
                    {
                    match(input,K_DOUBLE,FOLLOW_K_DOUBLE_in_native_type5750); 
                     t = CQL3Type.Native.DOUBLE; 

                    }
                    break;
                case 8 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:903:7: K_FLOAT
                    {
                    match(input,K_FLOAT,FOLLOW_K_FLOAT_in_native_type5763); 
                     t = CQL3Type.Native.FLOAT; 

                    }
                    break;
                case 9 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:904:7: K_INET
                    {
                    match(input,K_INET,FOLLOW_K_INET_in_native_type5777); 
                     t = CQL3Type.Native.INET;

                    }
                    break;
                case 10 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:905:7: K_INT
                    {
                    match(input,K_INT,FOLLOW_K_INT_in_native_type5792); 
                     t = CQL3Type.Native.INT; 

                    }
                    break;
                case 11 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:906:7: K_TEXT
                    {
                    match(input,K_TEXT,FOLLOW_K_TEXT_in_native_type5808); 
                     t = CQL3Type.Native.TEXT; 

                    }
                    break;
                case 12 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:907:7: K_TIMESTAMP
                    {
                    match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_native_type5823); 
                     t = CQL3Type.Native.TIMESTAMP; 

                    }
                    break;
                case 13 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:908:7: K_UUID
                    {
                    match(input,K_UUID,FOLLOW_K_UUID_in_native_type5833); 
                     t = CQL3Type.Native.UUID; 

                    }
                    break;
                case 14 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:909:7: K_VARCHAR
                    {
                    match(input,K_VARCHAR,FOLLOW_K_VARCHAR_in_native_type5848); 
                     t = CQL3Type.Native.VARCHAR; 

                    }
                    break;
                case 15 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:910:7: K_VARINT
                    {
                    match(input,K_VARINT,FOLLOW_K_VARINT_in_native_type5860); 
                     t = CQL3Type.Native.VARINT; 

                    }
                    break;
                case 16 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:911:7: K_TIMEUUID
                    {
                    match(input,K_TIMEUUID,FOLLOW_K_TIMEUUID_in_native_type5873); 
                     t = CQL3Type.Native.TIMEUUID; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return t;
    }
    // $ANTLR end "native_type"


    // $ANTLR start "collection_type"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:914:1: collection_type returns [CQL3Type pt] : ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' );
    public final CQL3Type collection_type() throws RecognitionException {
        CQL3Type pt = null;

        CQL3Type t1 = null;

        CQL3Type t2 = null;

        CQL3Type t = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:915:5: ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' )
            int alt107=3;
            switch ( input.LA(1) ) {
            case K_MAP:
                {
                alt107=1;
                }
                break;
            case K_LIST:
                {
                alt107=2;
                }
                break;
            case K_SET:
                {
                alt107=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 107, 0, input);

                throw nvae;
            }

            switch (alt107) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:915:7: K_MAP '<' t1= comparatorType ',' t2= comparatorType '>'
                    {
                    match(input,K_MAP,FOLLOW_K_MAP_in_collection_type5897); 
                    match(input,149,FOLLOW_149_in_collection_type5900); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type5904);
                    t1=comparatorType();

                    state._fsp--;

                    match(input,138,FOLLOW_138_in_collection_type5906); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type5910);
                    t2=comparatorType();

                    state._fsp--;

                    match(input,151,FOLLOW_151_in_collection_type5912); 
                     try {
                                // if we can't parse either t1 or t2, antlr will "recover" and we may have t1 or t2 null.
                                if (t1 != null && t2 != null)
                                    pt = CQL3Type.Collection.map(t1, t2);
                              } catch (InvalidRequestException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:921:7: K_LIST '<' t= comparatorType '>'
                    {
                    match(input,K_LIST,FOLLOW_K_LIST_in_collection_type5930); 
                    match(input,149,FOLLOW_149_in_collection_type5932); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type5936);
                    t=comparatorType();

                    state._fsp--;

                    match(input,151,FOLLOW_151_in_collection_type5938); 
                     try { if (t != null) pt = CQL3Type.Collection.list(t); } catch (InvalidRequestException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;
                case 3 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:923:7: K_SET '<' t= comparatorType '>'
                    {
                    match(input,K_SET,FOLLOW_K_SET_in_collection_type5956); 
                    match(input,149,FOLLOW_149_in_collection_type5959); 
                    pushFollow(FOLLOW_comparatorType_in_collection_type5963);
                    t=comparatorType();

                    state._fsp--;

                    match(input,151,FOLLOW_151_in_collection_type5965); 
                     try { if (t != null) pt = CQL3Type.Collection.set(t); } catch (InvalidRequestException e) { addRecognitionError(e.getMessage()); } 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return pt;
    }
    // $ANTLR end "collection_type"

    public static class username_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "username"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:927:1: username : ( IDENT | STRING_LITERAL );
    public final CqlParser.username_return username() throws RecognitionException {
        CqlParser.username_return retval = new CqlParser.username_return();
        retval.start = input.LT(1);

        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:928:5: ( IDENT | STRING_LITERAL )
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:
            {
            if ( input.LA(1)==IDENT||input.LA(1)==STRING_LITERAL ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "username"


    // $ANTLR start "unreserved_keyword"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:932:1: unreserved_keyword returns [String str] : (u= unreserved_function_keyword | k= ( K_TTL | K_COUNT | K_WRITETIME ) );
    public final String unreserved_keyword() throws RecognitionException {
        String str = null;

        Token k=null;
        String u = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:933:5: (u= unreserved_function_keyword | k= ( K_TTL | K_COUNT | K_WRITETIME ) )
            int alt108=2;
            int LA108_0 = input.LA(1);

            if ( (LA108_0==K_DISTINCT||LA108_0==K_AS||LA108_0==K_FILTERING||LA108_0==K_VALUES||LA108_0==K_EXISTS||LA108_0==K_TIMESTAMP||LA108_0==K_COUNTER||(LA108_0>=K_KEY && LA108_0<=K_CUSTOM)||LA108_0==K_TRIGGER||LA108_0==K_TYPE||LA108_0==K_LIST||(LA108_0>=K_ALL && LA108_0<=K_PASSWORD)||(LA108_0>=K_ASCII && LA108_0<=K_MAP)) ) {
                alt108=1;
            }
            else if ( (LA108_0==K_COUNT||(LA108_0>=K_WRITETIME && LA108_0<=K_TTL)) ) {
                alt108=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 108, 0, input);

                throw nvae;
            }
            switch (alt108) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:933:7: u= unreserved_function_keyword
                    {
                    pushFollow(FOLLOW_unreserved_function_keyword_in_unreserved_keyword6023);
                    u=unreserved_function_keyword();

                    state._fsp--;

                     str = u; 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:934:7: k= ( K_TTL | K_COUNT | K_WRITETIME )
                    {
                    k=(Token)input.LT(1);
                    if ( input.LA(1)==K_COUNT||(input.LA(1)>=K_WRITETIME && input.LA(1)<=K_TTL) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     str = (k!=null?k.getText():null); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "unreserved_keyword"


    // $ANTLR start "unreserved_function_keyword"
    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:937:1: unreserved_function_keyword returns [String str] : (k= ( K_KEY | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT ) | t= native_type );
    public final String unreserved_function_keyword() throws RecognitionException {
        String str = null;

        Token k=null;
        CQL3Type t = null;


        try {
            // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:938:5: (k= ( K_KEY | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT ) | t= native_type )
            int alt109=2;
            int LA109_0 = input.LA(1);

            if ( (LA109_0==K_DISTINCT||LA109_0==K_AS||LA109_0==K_FILTERING||LA109_0==K_VALUES||LA109_0==K_EXISTS||(LA109_0>=K_KEY && LA109_0<=K_CUSTOM)||LA109_0==K_TRIGGER||LA109_0==K_TYPE||LA109_0==K_LIST||(LA109_0>=K_ALL && LA109_0<=K_PASSWORD)||LA109_0==K_MAP) ) {
                alt109=1;
            }
            else if ( (LA109_0==K_TIMESTAMP||LA109_0==K_COUNTER||(LA109_0>=K_ASCII && LA109_0<=K_TIMEUUID)) ) {
                alt109=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 109, 0, input);

                throw nvae;
            }
            switch (alt109) {
                case 1 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:938:7: k= ( K_KEY | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_SUPERUSER | K_NOSUPERUSER | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT )
                    {
                    k=(Token)input.LT(1);
                    if ( input.LA(1)==K_DISTINCT||input.LA(1)==K_AS||input.LA(1)==K_FILTERING||input.LA(1)==K_VALUES||input.LA(1)==K_EXISTS||(input.LA(1)>=K_KEY && input.LA(1)<=K_CUSTOM)||input.LA(1)==K_TRIGGER||input.LA(1)==K_TYPE||input.LA(1)==K_LIST||(input.LA(1)>=K_ALL && input.LA(1)<=K_PASSWORD)||input.LA(1)==K_MAP ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                     str = (k!=null?k.getText():null); 

                    }
                    break;
                case 2 :
                    // /home/tonypan/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:962:7: t= native_type
                    {
                    pushFollow(FOLLOW_native_type_in_unreserved_function_keyword6362);
                    t=native_type();

                    state._fsp--;

                     str = t.toString(); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return str;
    }
    // $ANTLR end "unreserved_function_keyword"

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA76 dfa76 = new DFA76(this);
    protected DFA95 dfa95 = new DFA95(this);
    protected DFA104 dfa104 = new DFA104(this);
    static final String DFA2_eotS =
        "\35\uffff";
    static final String DFA2_eofS =
        "\35\uffff";
    static final String DFA2_minS =
        "\1\4\7\uffff\3\47\2\uffff\1\5\17\uffff";
    static final String DFA2_maxS =
        "\1\76\7\uffff\3\107\2\uffff\1\112\17\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\3\uffff\1\20\1\21\1\uffff\1"+
        "\10\1\11\1\23\1\27\1\12\1\13\1\14\1\15\1\25\1\30\1\16\1\17\1\24"+
        "\1\26\1\22";
    static final String DFA2_specialS =
        "\35\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\6\1\1\20\uffff\1\2\7\uffff\1\3\1\uffff\1\5\1\4\4\uffff\1"+
            "\10\16\uffff\1\11\1\12\4\uffff\1\7\1\13\1\14\1\15",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\16\1\uffff\1\17\5\uffff\2\22\3\uffff\1\21\22\uffff\1\20",
            "\1\23\1\uffff\1\24\6\uffff\1\25\3\uffff\1\27\22\uffff\1\26",
            "\1\31\1\uffff\1\30\35\uffff\1\32",
            "",
            "",
            "\1\34\40\uffff\1\34\16\uffff\2\34\12\uffff\3\34\6\uffff\1\33",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "186:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement | st2= insertStatement | st3= updateStatement | st4= batchStatement | st5= deleteStatement | st6= useStatement | st7= truncateStatement | st8= createKeyspaceStatement | st9= createTableStatement | st10= createIndexStatement | st11= dropKeyspaceStatement | st12= dropTableStatement | st13= dropIndexStatement | st14= alterTableStatement | st15= alterKeyspaceStatement | st16= grantStatement | st17= revokeStatement | st18= listPermissionsStatement | st19= createUserStatement | st20= alterUserStatement | st21= dropUserStatement | st22= listUsersStatement | st23= createTriggerStatement | st24= dropTriggerStatement );";
        }
    }
    static final String DFA14_eotS =
        "\31\uffff";
    static final String DFA14_eofS =
        "\31\uffff";
    static final String DFA14_minS =
        "\1\6\1\10\1\uffff\23\10\3\uffff";
    static final String DFA14_maxS =
        "\1\145\1\u008a\1\uffff\23\u008a\3\uffff";
    static final String DFA14_acceptS =
        "\2\uffff\1\1\23\uffff\1\4\1\2\1\3";
    static final String DFA14_specialS =
        "\31\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\3\1\2\1\3\6\uffff\1\3\1\24\1\25\6\uffff\1\3\2\uffff\1\3\1"+
            "\uffff\1\17\5\uffff\1\10\7\uffff\5\3\1\uffff\1\1\2\uffff\1\3"+
            "\2\uffff\1\3\6\uffff\1\3\4\uffff\11\3\1\2\10\uffff\1\26\1\uffff"+
            "\1\4\1\5\1\6\1\7\1\11\1\12\1\13\1\14\1\15\1\16\1\20\1\21\1\22"+
            "\1\23\1\3",
            "\2\2\176\uffff\1\26\2\2",
            "",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\26\2\2",
            "\2\2\176\uffff\1\27\2\2",
            "\2\2\176\uffff\1\30\2\2",
            "",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "263:1: unaliasedSelector returns [Selectable s] : (c= cident | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' | f= functionName args= selectionFunctionArgs );";
        }
    }
    static final String DFA34_eotS =
        "\27\uffff";
    static final String DFA34_eofS =
        "\27\uffff";
    static final String DFA34_minS =
        "\1\6\24\11\2\uffff";
    static final String DFA34_maxS =
        "\1\145\24\u008c\2\uffff";
    static final String DFA34_acceptS =
        "\25\uffff\1\2\1\1";
    static final String DFA34_specialS =
        "\27\uffff}>";
    static final String[] DFA34_transitionS = {
            "\1\3\1\24\1\3\6\uffff\1\3\2\24\6\uffff\1\3\2\uffff\1\3\1\uffff"+
            "\1\17\5\uffff\1\10\7\uffff\5\3\1\uffff\1\1\2\uffff\1\3\2\uffff"+
            "\1\3\6\uffff\1\3\4\uffff\11\3\1\2\12\uffff\1\4\1\5\1\6\1\7\1"+
            "\11\1\12\1\13\1\14\1\15\1\16\1\20\1\21\1\22\1\23\1\3",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "\1\26\u0080\uffff\1\26\1\uffff\1\25",
            "",
            ""
    };

    static final short[] DFA34_eot = DFA.unpackEncodedString(DFA34_eotS);
    static final short[] DFA34_eof = DFA.unpackEncodedString(DFA34_eofS);
    static final char[] DFA34_min = DFA.unpackEncodedStringToUnsignedChars(DFA34_minS);
    static final char[] DFA34_max = DFA.unpackEncodedStringToUnsignedChars(DFA34_maxS);
    static final short[] DFA34_accept = DFA.unpackEncodedString(DFA34_acceptS);
    static final short[] DFA34_special = DFA.unpackEncodedString(DFA34_specialS);
    static final short[][] DFA34_transition;

    static {
        int numStates = DFA34_transitionS.length;
        DFA34_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA34_transition[i] = DFA.unpackEncodedString(DFA34_transitionS[i]);
        }
    }

    class DFA34 extends DFA {

        public DFA34(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 34;
            this.eot = DFA34_eot;
            this.eof = DFA34_eof;
            this.min = DFA34_min;
            this.max = DFA34_max;
            this.accept = DFA34_accept;
            this.special = DFA34_special;
            this.transition = DFA34_transition;
        }
        public String getDescription() {
            return "394:1: deleteOp returns [Operation.RawDeletion op] : (c= cident | c= cident '[' t= term ']' );";
        }
    }
    static final String DFA76_eotS =
        "\27\uffff";
    static final String DFA76_eofS =
        "\1\uffff\24\26\2\uffff";
    static final String DFA76_minS =
        "\1\6\24\11\2\uffff";
    static final String DFA76_maxS =
        "\1\145\24\u008e\2\uffff";
    static final String DFA76_acceptS =
        "\25\uffff\1\1\1\2";
    static final String DFA76_specialS =
        "\27\uffff}>";
    static final String[] DFA76_transitionS = {
            "\1\3\1\24\1\3\6\uffff\1\3\2\24\6\uffff\1\3\2\uffff\1\3\1\uffff"+
            "\1\17\5\uffff\1\10\7\uffff\5\3\1\uffff\1\1\2\uffff\1\3\2\uffff"+
            "\1\3\6\uffff\1\3\4\uffff\11\3\1\2\12\uffff\1\4\1\5\1\6\1\7\1"+
            "\11\1\12\1\13\1\14\1\15\1\16\1\20\1\21\1\22\1\23\1\3",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "\3\26\1\uffff\2\26\15\uffff\1\26\2\uffff\1\26\10\uffff\1\26"+
            "\14\uffff\2\26\1\uffff\3\26\4\uffff\2\26\106\uffff\2\26\5\uffff"+
            "\1\25",
            "",
            ""
    };

    static final short[] DFA76_eot = DFA.unpackEncodedString(DFA76_eotS);
    static final short[] DFA76_eof = DFA.unpackEncodedString(DFA76_eofS);
    static final char[] DFA76_min = DFA.unpackEncodedStringToUnsignedChars(DFA76_minS);
    static final char[] DFA76_max = DFA.unpackEncodedStringToUnsignedChars(DFA76_maxS);
    static final short[] DFA76_accept = DFA.unpackEncodedString(DFA76_acceptS);
    static final short[] DFA76_special = DFA.unpackEncodedString(DFA76_specialS);
    static final short[][] DFA76_transition;

    static {
        int numStates = DFA76_transitionS.length;
        DFA76_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA76_transition[i] = DFA.unpackEncodedString(DFA76_transitionS[i]);
        }
    }

    class DFA76 extends DFA {

        public DFA76(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 76;
            this.eot = DFA76_eot;
            this.eof = DFA76_eof;
            this.min = DFA76_min;
            this.max = DFA76_max;
            this.accept = DFA76_accept;
            this.special = DFA76_special;
            this.transition = DFA76_transition;
        }
        public String getDescription() {
            return "729:7: ( cfOrKsName[name, true] '.' )?";
        }
    }
    static final String DFA95_eotS =
        "\56\uffff";
    static final String DFA95_eofS =
        "\56\uffff";
    static final String DFA95_minS =
        "\1\6\24\u008c\1\6\2\uffff\24\22\2\uffff";
    static final String DFA95_maxS =
        "\1\145\24\u0093\1\u0091\2\uffff\24\u0094\2\uffff";
    static final String DFA95_acceptS =
        "\26\uffff\1\4\1\1\24\uffff\1\2\1\3";
    static final String DFA95_specialS =
        "\56\uffff}>";
    static final String[] DFA95_transitionS = {
            "\1\3\1\24\1\3\6\uffff\1\3\2\24\6\uffff\1\3\2\uffff\1\3\1\uffff"+
            "\1\17\5\uffff\1\10\7\uffff\5\3\1\uffff\1\1\2\uffff\1\3\2\uffff"+
            "\1\3\6\uffff\1\3\4\uffff\11\3\1\2\12\uffff\1\4\1\5\1\6\1\7\1"+
            "\11\1\12\1\13\1\14\1\15\1\16\1\20\1\21\1\22\1\23\1\3",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\26\6\uffff\1\25",
            "\1\31\1\53\1\31\6\uffff\1\31\2\53\1\27\5\uffff\1\31\2\uffff"+
            "\1\31\1\uffff\1\45\5\uffff\1\36\7\uffff\5\31\1\uffff\1\30\1"+
            "\uffff\1\27\1\31\2\uffff\1\31\6\uffff\1\31\4\uffff\11\31\1\52"+
            "\11\27\1\uffff\1\32\1\33\1\34\1\35\1\37\1\40\1\41\1\42\1\43"+
            "\1\44\1\46\1\47\1\50\1\51\1\31\42\uffff\1\27\3\uffff\1\27\2"+
            "\uffff\3\27",
            "",
            "",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\165\uffff\1\27\6\uffff\1\54\4\uffff\1\54",
            "\1\55\174\uffff\1\54\4\uffff\1\54",
            "\1\55\174\uffff\1\54\4\uffff\1\54",
            "",
            ""
    };

    static final short[] DFA95_eot = DFA.unpackEncodedString(DFA95_eotS);
    static final short[] DFA95_eof = DFA.unpackEncodedString(DFA95_eofS);
    static final char[] DFA95_min = DFA.unpackEncodedStringToUnsignedChars(DFA95_minS);
    static final char[] DFA95_max = DFA.unpackEncodedStringToUnsignedChars(DFA95_maxS);
    static final short[] DFA95_accept = DFA.unpackEncodedString(DFA95_acceptS);
    static final short[] DFA95_special = DFA.unpackEncodedString(DFA95_specialS);
    static final short[][] DFA95_transition;

    static {
        int numStates = DFA95_transitionS.length;
        DFA95_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA95_transition[i] = DFA.unpackEncodedString(DFA95_transitionS[i]);
        }
    }

    class DFA95 extends DFA {

        public DFA95(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 95;
            this.eot = DFA95_eot;
            this.eof = DFA95_eof;
            this.min = DFA95_min;
            this.max = DFA95_max;
            this.accept = DFA95_accept;
            this.special = DFA95_special;
            this.transition = DFA95_transition;
        }
        public String getDescription() {
            return "807:1: columnOperation[List<Pair<ColumnIdentifier, Operation.RawUpdate>> operations] : (key= cident '=' t= term ( '+' c= cident )? | key= cident '=' c= cident sig= ( '+' | '-' ) t= term | key= cident '=' c= cident i= INTEGER | key= cident '[' k= term ']' '=' t= term );";
        }
    }
    static final String DFA104_eotS =
        "\33\uffff";
    static final String DFA104_eofS =
        "\33\uffff";
    static final String DFA104_minS =
        "\1\6\24\126\3\uffff\1\124\2\uffff";
    static final String DFA104_maxS =
        "\1\u0088\24\u0098\3\uffff\1\u0091\2\uffff";
    static final String DFA104_acceptS =
        "\25\uffff\1\2\1\5\1\1\1\uffff\1\4\1\3";
    static final String DFA104_specialS =
        "\33\uffff}>";
    static final String[] DFA104_transitionS = {
            "\1\3\1\24\1\3\6\uffff\1\3\2\24\6\uffff\1\3\2\uffff\1\3\1\uffff"+
            "\1\17\5\uffff\1\10\7\uffff\5\3\1\uffff\1\1\2\uffff\1\3\2\uffff"+
            "\1\3\6\uffff\1\3\4\uffff\11\3\1\2\10\uffff\1\25\1\uffff\1\4"+
            "\1\5\1\6\1\7\1\11\1\12\1\13\1\14\1\15\1\16\1\20\1\21\1\22\1"+
            "\23\1\3\42\uffff\1\26",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "\1\30\74\uffff\1\27\1\uffff\4\27",
            "",
            "",
            "",
            "\1\32\63\uffff\1\31\10\uffff\1\32",
            "",
            ""
    };

    static final short[] DFA104_eot = DFA.unpackEncodedString(DFA104_eotS);
    static final short[] DFA104_eof = DFA.unpackEncodedString(DFA104_eofS);
    static final char[] DFA104_min = DFA.unpackEncodedStringToUnsignedChars(DFA104_minS);
    static final char[] DFA104_max = DFA.unpackEncodedStringToUnsignedChars(DFA104_maxS);
    static final short[] DFA104_accept = DFA.unpackEncodedString(DFA104_acceptS);
    static final short[] DFA104_special = DFA.unpackEncodedString(DFA104_specialS);
    static final short[][] DFA104_transition;

    static {
        int numStates = DFA104_transitionS.length;
        DFA104_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA104_transition[i] = DFA.unpackEncodedString(DFA104_transitionS[i]);
        }
    }

    class DFA104 extends DFA {

        public DFA104(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 104;
            this.eot = DFA104_eot;
            this.eof = DFA104_eof;
            this.min = DFA104_min;
            this.max = DFA104_max;
            this.accept = DFA104_accept;
            this.special = DFA104_special;
            this.transition = DFA104_transition;
        }
        public String getDescription() {
            return "863:1: relation[List<Relation> clauses] : (name= cident type= relationType t= term | K_TOKEN '(' name1= cident ( ',' namen= cident )* ')' type= relationType t= term | name= cident K_IN ( QMARK | ':' mid= cident ) | name= cident K_IN '(' (f1= term ( ',' fN= term )* )? ')' | '(' relation[$clauses] ')' );";
        }
    }
 

    public static final BitSet FOLLOW_cqlStatement_in_query72 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_135_in_query75 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_EOF_in_query79 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectStatement_in_cqlStatement113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_insertStatement_in_cqlStatement138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateStatement_in_cqlStatement163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_batchStatement_in_cqlStatement188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteStatement_in_cqlStatement214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_useStatement_in_cqlStatement239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_truncateStatement_in_cqlStatement267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createKeyspaceStatement_in_cqlStatement290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createTableStatement_in_cqlStatement307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createIndexStatement_in_cqlStatement326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropKeyspaceStatement_in_cqlStatement345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropTableStatement_in_cqlStatement363 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropIndexStatement_in_cqlStatement384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alterTableStatement_in_cqlStatement405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alterKeyspaceStatement_in_cqlStatement425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grantStatement_in_cqlStatement442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_revokeStatement_in_cqlStatement467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listPermissionsStatement_in_cqlStatement491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createUserStatement_in_cqlStatement506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alterUserStatement_in_cqlStatement526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropUserStatement_in_cqlStatement547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listUsersStatement_in_cqlStatement569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createTriggerStatement_in_cqlStatement590 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropTriggerStatement_in_cqlStatement607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_USE_in_useStatement639 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_keyspaceName_in_useStatement643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_SELECT_in_selectStatement677 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L,0x0000000000000800L});
    public static final BitSet FOLLOW_K_DISTINCT_in_selectStatement683 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L,0x0000000000000800L});
    public static final BitSet FOLLOW_selectClause_in_selectStatement692 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_K_COUNT_in_selectStatement712 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_selectStatement714 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_selectCountClause_in_selectStatement718 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_selectStatement720 = new BitSet(new long[]{0x0000000000000300L});
    public static final BitSet FOLLOW_K_AS_in_selectStatement725 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_selectStatement729 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_K_FROM_in_selectStatement744 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_selectStatement748 = new BitSet(new long[]{0x0000000000006C02L});
    public static final BitSet FOLLOW_K_WHERE_in_selectStatement758 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L,0x0000000000000100L});
    public static final BitSet FOLLOW_whereClause_in_selectStatement762 = new BitSet(new long[]{0x0000000000006802L});
    public static final BitSet FOLLOW_K_ORDER_in_selectStatement775 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_K_BY_in_selectStatement777 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_orderByClause_in_selectStatement779 = new BitSet(new long[]{0x0000000000006002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_138_in_selectStatement784 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_orderByClause_in_selectStatement786 = new BitSet(new long[]{0x0000000000006002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_K_LIMIT_in_selectStatement803 = new BitSet(new long[]{0x0000000000044000L,0x0000000000100000L,0x0000000000020000L});
    public static final BitSet FOLLOW_intValue_in_selectStatement807 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_K_ALLOW_in_selectStatement822 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_K_FILTERING_in_selectStatement824 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_selectClause861 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_138_in_selectClause866 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L});
    public static final BitSet FOLLOW_selector_in_selectClause870 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_139_in_selectClause882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaliasedSelector_in_selector915 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_K_AS_in_selector918 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_selector922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_unaliasedSelector951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_WRITETIME_in_unaliasedSelector994 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_unaliasedSelector996 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_unaliasedSelector1000 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_unaliasedSelector1002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TTL_in_unaliasedSelector1025 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_unaliasedSelector1033 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_unaliasedSelector1037 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_unaliasedSelector1039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_unaliasedSelector1064 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_selectionFunctionArgs_in_unaliasedSelector1068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_selectionFunctionArgs1091 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_selectionFunctionArgs1093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_selectionFunctionArgs1103 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L});
    public static final BitSet FOLLOW_unaliasedSelector_in_selectionFunctionArgs1107 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_selectionFunctionArgs1123 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L});
    public static final BitSet FOLLOW_unaliasedSelector_in_selectionFunctionArgs1127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_selectionFunctionArgs1141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_139_in_selectCountClause1164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_selectCountClause1186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_in_whereClause1222 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_AND_in_whereClause1226 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L,0x0000000000000100L});
    public static final BitSet FOLLOW_relation_in_whereClause1228 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_cident_in_orderByClause1259 = new BitSet(new long[]{0x0000000000300002L});
    public static final BitSet FOLLOW_K_ASC_in_orderByClause1264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DESC_in_orderByClause1268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_INSERT_in_insertStatement1306 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_K_INTO_in_insertStatement1308 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_insertStatement1312 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_insertStatement1324 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_insertStatement1328 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_insertStatement1335 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_insertStatement1339 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_insertStatement1346 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_K_VALUES_in_insertStatement1356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_insertStatement1368 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_insertStatement1372 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_insertStatement1378 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_insertStatement1382 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_insertStatement1389 = new BitSet(new long[]{0x0000000012000002L});
    public static final BitSet FOLLOW_K_IF_in_insertStatement1402 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_K_NOT_in_insertStatement1404 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_K_EXISTS_in_insertStatement1406 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_usingClause_in_insertStatement1423 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_USING_in_usingClause1453 = new BitSet(new long[]{0x0000000020020000L});
    public static final BitSet FOLLOW_usingClauseObjective_in_usingClause1455 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_AND_in_usingClause1460 = new BitSet(new long[]{0x0000000020020000L});
    public static final BitSet FOLLOW_usingClauseObjective_in_usingClause1462 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_TIMESTAMP_in_usingClauseObjective1484 = new BitSet(new long[]{0x0000000000040000L,0x0000000000100000L,0x0000000000020000L});
    public static final BitSet FOLLOW_intValue_in_usingClauseObjective1488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TTL_in_usingClauseObjective1498 = new BitSet(new long[]{0x0000000000040000L,0x0000000000100000L,0x0000000000020000L});
    public static final BitSet FOLLOW_intValue_in_usingClauseObjective1502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_UPDATE_in_updateStatement1536 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_updateStatement1540 = new BitSet(new long[]{0x0000000090000000L});
    public static final BitSet FOLLOW_usingClause_in_updateStatement1550 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_K_SET_in_updateStatement1562 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnOperation_in_updateStatement1564 = new BitSet(new long[]{0x0000000000000400L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_138_in_updateStatement1568 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnOperation_in_updateStatement1570 = new BitSet(new long[]{0x0000000000000400L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_K_WHERE_in_updateStatement1581 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L,0x0000000000000100L});
    public static final BitSet FOLLOW_whereClause_in_updateStatement1585 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_K_IF_in_updateStatement1595 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_updateCondition_in_updateStatement1599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnOperation_in_updateCondition1640 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_AND_in_updateCondition1645 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnOperation_in_updateCondition1647 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_DELETE_in_deleteStatement1683 = new BitSet(new long[]{0x4092F808290383C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_deleteSelection_in_deleteStatement1689 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_K_FROM_in_deleteStatement1702 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_deleteStatement1706 = new BitSet(new long[]{0x0000000010000400L});
    public static final BitSet FOLLOW_usingClauseDelete_in_deleteStatement1716 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_K_WHERE_in_deleteStatement1728 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L,0x0000000000000100L});
    public static final BitSet FOLLOW_whereClause_in_deleteStatement1732 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_K_IF_in_deleteStatement1742 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_updateCondition_in_deleteStatement1746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteOp_in_deleteSelection1792 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_138_in_deleteSelection1807 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_deleteOp_in_deleteSelection1811 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_cident_in_deleteOp1838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_deleteOp1865 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_140_in_deleteOp1867 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_deleteOp1871 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_141_in_deleteOp1873 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_USING_in_usingClauseDelete1893 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_K_TIMESTAMP_in_usingClauseDelete1895 = new BitSet(new long[]{0x0000000000040000L,0x0000000000100000L,0x0000000000020000L});
    public static final BitSet FOLLOW_intValue_in_usingClauseDelete1899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BEGIN_in_batchStatement1933 = new BitSet(new long[]{0x0000001C00000000L});
    public static final BitSet FOLLOW_K_UNLOGGED_in_batchStatement1943 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_K_COUNTER_in_batchStatement1949 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_K_BATCH_in_batchStatement1962 = new BitSet(new long[]{0x0000002150400000L});
    public static final BitSet FOLLOW_usingClause_in_batchStatement1966 = new BitSet(new long[]{0x0000002140400000L});
    public static final BitSet FOLLOW_batchStatementObjective_in_batchStatement1986 = new BitSet(new long[]{0x0000002140400000L,0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_135_in_batchStatement1988 = new BitSet(new long[]{0x0000002140400000L});
    public static final BitSet FOLLOW_K_APPLY_in_batchStatement2002 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_K_BATCH_in_batchStatement2004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_insertStatement_in_batchStatementObjective2035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_updateStatement_in_batchStatementObjective2048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_deleteStatement_in_batchStatementObjective2061 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createKeyspaceStatement2096 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_K_KEYSPACE_in_createKeyspaceStatement2098 = new BitSet(new long[]{0x4092F8082B0381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_K_IF_in_createKeyspaceStatement2101 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_K_NOT_in_createKeyspaceStatement2103 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_K_EXISTS_in_createKeyspaceStatement2105 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_keyspaceName_in_createKeyspaceStatement2114 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_K_WITH_in_createKeyspaceStatement2122 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_properties_in_createKeyspaceStatement2124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createTableStatement2159 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_K_COLUMNFAMILY_in_createTableStatement2161 = new BitSet(new long[]{0x4092F8082B0381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_K_IF_in_createTableStatement2164 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_K_NOT_in_createTableStatement2166 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_K_EXISTS_in_createTableStatement2168 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_createTableStatement2183 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_cfamDefinition_in_createTableStatement2193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_cfamDefinition2212 = new BitSet(new long[]{0x4092FC08290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition2214 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_cfamDefinition2219 = new BitSet(new long[]{0x4092FC08290381C0L,0x0000003FFF801FF8L,0x0000000000000600L});
    public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition2221 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_cfamDefinition2228 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_K_WITH_in_cfamDefinition2238 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition2240 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_AND_in_cfamDefinition2245 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition2247 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_cident_in_cfamColumns2273 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_cfamColumns2277 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns2282 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_K_KEY_in_cfamColumns2284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns2296 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_K_KEY_in_cfamColumns2298 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_cfamColumns2300 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L,0x0000000000000100L});
    public static final BitSet FOLLOW_pkDef_in_cfamColumns2302 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_cfamColumns2306 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_cfamColumns2310 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_cfamColumns2317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_pkDef2337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_pkDef2347 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_pkDef2353 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_pkDef2359 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_pkDef2363 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_pkDef2370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_cfamProperty2390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_COMPACT_in_cfamProperty2399 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_K_STORAGE_in_cfamProperty2401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CLUSTERING_in_cfamProperty2411 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_K_ORDER_in_cfamProperty2413 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_K_BY_in_cfamProperty2415 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_cfamProperty2417 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cfamOrdering_in_cfamProperty2419 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_cfamProperty2423 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cfamOrdering_in_cfamProperty2425 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_cfamProperty2430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_cfamOrdering2458 = new BitSet(new long[]{0x0000000000300000L});
    public static final BitSet FOLLOW_K_ASC_in_cfamOrdering2461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DESC_in_cfamOrdering2465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createIndexStatement2503 = new BitSet(new long[]{0x0001800000000000L});
    public static final BitSet FOLLOW_K_CUSTOM_in_createIndexStatement2506 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_K_INDEX_in_createIndexStatement2512 = new BitSet(new long[]{0x0006000002000000L});
    public static final BitSet FOLLOW_K_IF_in_createIndexStatement2515 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_K_NOT_in_createIndexStatement2517 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_K_EXISTS_in_createIndexStatement2519 = new BitSet(new long[]{0x0006000000000000L});
    public static final BitSet FOLLOW_IDENT_in_createIndexStatement2537 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_K_ON_in_createIndexStatement2541 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_createIndexStatement2545 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_createIndexStatement2547 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_createIndexStatement2551 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_createIndexStatement2553 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_K_USING_in_createIndexStatement2565 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_createIndexStatement2569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createTriggerStatement2603 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_K_TRIGGER_in_createTriggerStatement2605 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_IDENT_in_createTriggerStatement2610 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_K_ON_in_createTriggerStatement2613 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_createTriggerStatement2617 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_K_USING_in_createTriggerStatement2619 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_createTriggerStatement2623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_dropTriggerStatement2654 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_K_TRIGGER_in_dropTriggerStatement2656 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_IDENT_in_dropTriggerStatement2661 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_K_ON_in_dropTriggerStatement2664 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_dropTriggerStatement2668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ALTER_in_alterKeyspaceStatement2708 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_K_KEYSPACE_in_alterKeyspaceStatement2710 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_keyspaceName_in_alterKeyspaceStatement2714 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_K_WITH_in_alterKeyspaceStatement2724 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_properties_in_alterKeyspaceStatement2726 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement2762 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_K_COLUMNFAMILY_in_alterTableStatement2764 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_alterTableStatement2768 = new BitSet(new long[]{0x0360010000000000L});
    public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement2782 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement2786 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_K_TYPE_in_alterTableStatement2788 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_alterTableStatement2792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ADD_in_alterTableStatement2808 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement2814 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_alterTableStatement2818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_alterTableStatement2841 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement2846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_WITH_in_alterTableStatement2886 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_properties_in_alterTableStatement2889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_RENAME_in_alterTableStatement2922 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement2976 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_K_TO_in_alterTableStatement2978 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement2982 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_AND_in_alterTableStatement3003 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement3007 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_K_TO_in_alterTableStatement3009 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_alterTableStatement3013 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_DROP_in_dropKeyspaceStatement3068 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement3070 = new BitSet(new long[]{0x4092F8082B0381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_K_IF_in_dropKeyspaceStatement3073 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_K_EXISTS_in_dropKeyspaceStatement3075 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_keyspaceName_in_dropKeyspaceStatement3084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_dropTableStatement3118 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_K_COLUMNFAMILY_in_dropTableStatement3120 = new BitSet(new long[]{0x4092F8082B0381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_K_IF_in_dropTableStatement3123 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_K_EXISTS_in_dropTableStatement3125 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_dropTableStatement3134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_dropIndexStatement3168 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_K_INDEX_in_dropIndexStatement3170 = new BitSet(new long[]{0x0002000002000000L});
    public static final BitSet FOLLOW_K_IF_in_dropIndexStatement3173 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_K_EXISTS_in_dropIndexStatement3175 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_IDENT_in_dropIndexStatement3184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TRUNCATE_in_truncateStatement3215 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_truncateStatement3219 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_GRANT_in_grantStatement3244 = new BitSet(new long[]{0x0060004000000020L,0x000000000000000EL});
    public static final BitSet FOLLOW_permissionOrAll_in_grantStatement3256 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_K_ON_in_grantStatement3264 = new BitSet(new long[]{0x4092FA88290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_resource_in_grantStatement3276 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_K_TO_in_grantStatement3284 = new BitSet(new long[]{0x000A000000000000L});
    public static final BitSet FOLLOW_username_in_grantStatement3296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_REVOKE_in_revokeStatement3327 = new BitSet(new long[]{0x0060004000000020L,0x000000000000000EL});
    public static final BitSet FOLLOW_permissionOrAll_in_revokeStatement3339 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_K_ON_in_revokeStatement3347 = new BitSet(new long[]{0x4092FA88290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_resource_in_revokeStatement3359 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_K_FROM_in_revokeStatement3367 = new BitSet(new long[]{0x000A000000000000L});
    public static final BitSet FOLLOW_username_in_revokeStatement3379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_LIST_in_listPermissionsStatement3417 = new BitSet(new long[]{0x0060004000000020L,0x000000000000000EL});
    public static final BitSet FOLLOW_permissionOrAll_in_listPermissionsStatement3429 = new BitSet(new long[]{0x8004000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_K_ON_in_listPermissionsStatement3439 = new BitSet(new long[]{0x4092FA88290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_resource_in_listPermissionsStatement3441 = new BitSet(new long[]{0x8000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_K_OF_in_listPermissionsStatement3456 = new BitSet(new long[]{0x000A000000000000L});
    public static final BitSet FOLLOW_username_in_listPermissionsStatement3458 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_K_NORECURSIVE_in_listPermissionsStatement3473 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_permission3509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ALL_in_permissionOrAll3558 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_K_PERMISSIONS_in_permissionOrAll3562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_permission_in_permissionOrAll3583 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_K_PERMISSION_in_permissionOrAll3587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dataResource_in_resource3615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ALL_in_dataResource3638 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_K_KEYSPACES_in_dataResource3640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_KEYSPACE_in_dataResource3650 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_keyspaceName_in_dataResource3656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_COLUMNFAMILY_in_dataResource3668 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_columnFamilyName_in_dataResource3677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_CREATE_in_createUserStatement3717 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_K_USER_in_createUserStatement3719 = new BitSet(new long[]{0x000A000000000000L});
    public static final BitSet FOLLOW_username_in_createUserStatement3721 = new BitSet(new long[]{0x0000010000000002L,0x0000000000000300L});
    public static final BitSet FOLLOW_K_WITH_in_createUserStatement3731 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_userOptions_in_createUserStatement3733 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000300L});
    public static final BitSet FOLLOW_K_SUPERUSER_in_createUserStatement3747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_NOSUPERUSER_in_createUserStatement3753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ALTER_in_alterUserStatement3798 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_K_USER_in_alterUserStatement3800 = new BitSet(new long[]{0x000A000000000000L});
    public static final BitSet FOLLOW_username_in_alterUserStatement3802 = new BitSet(new long[]{0x0000010000000002L,0x0000000000000300L});
    public static final BitSet FOLLOW_K_WITH_in_alterUserStatement3812 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_userOptions_in_alterUserStatement3814 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000300L});
    public static final BitSet FOLLOW_K_SUPERUSER_in_alterUserStatement3828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_NOSUPERUSER_in_alterUserStatement3834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DROP_in_dropUserStatement3870 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_K_USER_in_dropUserStatement3872 = new BitSet(new long[]{0x000A000000000000L});
    public static final BitSet FOLLOW_username_in_dropUserStatement3874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_LIST_in_listUsersStatement3899 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_K_USERS_in_listUsersStatement3901 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_userOption_in_userOptions3921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_PASSWORD_in_userOption3942 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_userOption3946 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cident3975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_NAME_in_cident4000 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_keyword_in_cident4019 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cfOrKsName_in_keyspaceName4052 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cfOrKsName_in_columnFamilyName4086 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_142_in_columnFamilyName4089 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cfOrKsName_in_columnFamilyName4093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_cfOrKsName4114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_NAME_in_cfOrKsName4139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_keyword_in_cfOrKsName4158 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_constant4183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_constant4195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_constant4214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOLEAN_in_constant4235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UUID_in_constant4254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HEXNUMBER_in_constant4276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_143_in_constant4294 = new BitSet(new long[]{0x0000000000000000L,0x0000000000060000L});
    public static final BitSet FOLLOW_set_in_constant4303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_144_in_map_literal4332 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000079100L});
    public static final BitSet FOLLOW_term_in_map_literal4350 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_145_in_map_literal4352 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_map_literal4356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040400L});
    public static final BitSet FOLLOW_138_in_map_literal4362 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_map_literal4366 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_145_in_map_literal4368 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_map_literal4372 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040400L});
    public static final BitSet FOLLOW_146_in_map_literal4388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_145_in_set_or_map4412 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_set_or_map4416 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_138_in_set_or_map4432 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_set_or_map4436 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_145_in_set_or_map4438 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_set_or_map4442 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_138_in_set_or_map4477 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_set_or_map4481 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_140_in_collection_literal4515 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x000000000003B100L});
    public static final BitSet FOLLOW_term_in_collection_literal4533 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002400L});
    public static final BitSet FOLLOW_138_in_collection_literal4539 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_collection_literal4543 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002400L});
    public static final BitSet FOLLOW_141_in_collection_literal4559 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_144_in_collection_literal4569 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_collection_literal4573 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000060400L});
    public static final BitSet FOLLOW_set_or_map_in_collection_literal4577 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_146_in_collection_literal4582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_144_in_collection_literal4600 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_146_in_collection_literal4602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_value4627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_literal_in_value4649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_NULL_in_value4659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_145_in_value4683 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_value4687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QMARK_in_value4704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_intValue4750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_145_in_intValue4764 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_intValue4768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QMARK_in_intValue4778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName4811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_function_keyword_in_functionName4845 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TOKEN_in_functionName4855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_functionArgs4900 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_functionArgs4902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_functionArgs4912 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_functionArgs4916 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_functionArgs4932 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_functionArgs4936 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_functionArgs4950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_in_term4975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_term5012 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_functionArgs_in_term5016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_term5026 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_term5030 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_term5032 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_term5036 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation5059 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_147_in_columnOperation5061 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_columnOperation5065 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_148_in_columnOperation5068 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_columnOperation5072 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation5093 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_147_in_columnOperation5095 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_columnOperation5099 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000108000L});
    public static final BitSet FOLLOW_set_in_columnOperation5103 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_columnOperation5113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation5131 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_147_in_columnOperation5133 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_columnOperation5137 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_INTEGER_in_columnOperation5141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_columnOperation5159 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_140_in_columnOperation5161 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_columnOperation5165 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_141_in_columnOperation5167 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_147_in_columnOperation5169 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_columnOperation5173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_properties5199 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_K_AND_in_properties5203 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_property_in_properties5205 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_cident_in_property5228 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_147_in_property5230 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFF87FFF8L,0x0000000000018000L});
    public static final BitSet FOLLOW_propertyValue_in_property5235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_map_literal_in_property5264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_propertyValue5292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_keyword_in_propertyValue5314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_147_in_relationType5337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_149_in_relationType5348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_150_in_relationType5359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_151_in_relationType5369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_152_in_relationType5380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_relation5402 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000001E80000L});
    public static final BitSet FOLLOW_relationType_in_relation5406 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_relation5410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TOKEN_in_relation5420 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_relation5443 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_relation5447 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_relation5453 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_relation5457 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_relation5463 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000001E80000L});
    public static final BitSet FOLLOW_relationType_in_relation5475 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_relation5479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_relation5499 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_K_IN_in_relation5501 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L,0x0000000000020000L});
    public static final BitSet FOLLOW_QMARK_in_relation5506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_145_in_relation5512 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFF801FF8L});
    public static final BitSet FOLLOW_cident_in_relation5516 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cident_in_relation5539 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_K_IN_in_relation5541 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_136_in_relation5552 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039300L});
    public static final BitSet FOLLOW_term_in_relation5558 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_138_in_relation5563 = new BitSet(new long[]{0x409AF808290781C0L,0x0000003FFFBFFFF8L,0x0000000000039100L});
    public static final BitSet FOLLOW_term_in_relation5567 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000600L});
    public static final BitSet FOLLOW_137_in_relation5577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_136_in_relation5587 = new BitSet(new long[]{0x4092F808290381C0L,0x0000003FFFA01FF8L,0x0000000000000100L});
    public static final BitSet FOLLOW_relation_in_relation5589 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_137_in_relation5592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_native_type_in_comparatorType5615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_type_in_comparatorType5631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_comparatorType5643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_ASCII_in_native_type5672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BIGINT_in_native_type5686 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BLOB_in_native_type5699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_BOOLEAN_in_native_type5714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_COUNTER_in_native_type5726 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DECIMAL_in_native_type5738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_DOUBLE_in_native_type5750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_FLOAT_in_native_type5763 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_INET_in_native_type5777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_INT_in_native_type5792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TEXT_in_native_type5808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TIMESTAMP_in_native_type5823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_UUID_in_native_type5833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_VARCHAR_in_native_type5848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_VARINT_in_native_type5860 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_TIMEUUID_in_native_type5873 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_MAP_in_collection_type5897 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_149_in_collection_type5900 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type5904 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_138_in_collection_type5906 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type5910 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_151_in_collection_type5912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_LIST_in_collection_type5930 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_149_in_collection_type5932 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type5936 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_151_in_collection_type5938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_K_SET_in_collection_type5956 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_149_in_collection_type5959 = new BitSet(new long[]{0x4098F808A9008140L,0x0000003FFF800FF8L});
    public static final BitSet FOLLOW_comparatorType_in_collection_type5963 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_151_in_collection_type5965 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_username0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unreserved_function_keyword_in_unreserved_keyword6023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unreserved_keyword6039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unreserved_function_keyword6074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_native_type_in_unreserved_function_keyword6362 = new BitSet(new long[]{0x0000000000000002L});

}