package nih.nlm.solr.customfilter;

import gravel.norm.CharNorm;
import gravel.norm.TermNorm;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public final class EssieTokenizerFilter extends TokenFilter
{

    static Logger LOG = Logger.getLogger(EssieTokenizerFilter.class.getName());

    private LinkedList<String> tokens;
    
    private int lastIndex = 0;
    private String tokenizerText;

    private final CharTermAttribute charTermAttr = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);


    protected EssieTokenizerFilter(TokenStream input)
    {
        super(input);

    }

    @Override
    public boolean incrementToken() throws IOException
    {     

        if( tokens != null && tokens.isEmpty() )
        {            
            tokens = null;
            return false;
        }
        else if( tokens == null )
        {
            if (!input.incrementToken())
            {
                return false;
            }
            String text = charTermAttr.toString();
            try
            {
                tokens = (LinkedList)generateEssieToken(text);
                lastIndex = 0;
                tokenizerText = text;

            }
            catch( Exception e )
            {
                LOG.log(Level.SEVERE, "Error in genrating Essie Token", e);
                return false;
            }
        }
        
        
        if( tokens != null && !tokens.isEmpty())
        {
            String term = tokens.removeFirst();
            CharTermAttribute charTermAttributeLocal =  charTermAttr.setEmpty();
            charTermAttributeLocal.append(term);
            charTermAttributeLocal.setLength(term.length() );
            
            int termPosition = tokenizerText.toLowerCase().indexOf(term, lastIndex);
            if( termPosition >= lastIndex )
            {
                lastIndex = termPosition;
            }
            else
            {
                return true;
            }
            
            offsetAtt.setOffset(lastIndex, lastIndex+term.length() );
            lastIndex = lastIndex + 1;
                                   
            return true;
        }
            
        return false;
    }

    @Override
    public void reset() throws IOException
    {
        super.reset();
    }
    
    @Override
    public void end() throws IOException {
        super.end();
    }
    
    @Override
    public void close() throws IOException {
        super.close();
    }
        
    public static List<String> generateEssieToken( String input ) throws Exception
    {
        /*
        Removing all the new line chracters and tabs.
        */
        input = input.replaceAll("\\\\n"," ").replaceAll("\\\\t"," ").replaceAll("\\\\r"," ");
        
        long startTime = System.currentTimeMillis();
        
        LinkedList<String> list = new LinkedList<>();
                
        String kIndexNormDir = "";
        if(System.getProperty("os.name").startsWith("Windows"))
        {
            kIndexNormDir = "Z:/team_directories/Soumya/SearchEngineEvaluation/essie-v4/refs/norm/final";
        }
        else
        {
            String jarPath = EssieTokenizerFilter.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            
            kIndexNormDir = (new File(jarPath)).getParent()+File.separator+"norm";
            
        }

        CharNorm.load(kIndexNormDir);
        TermNorm.load(kIndexNormDir);
        
        String[] theNormTokenss=TermNorm.getNormTokens( input );
        
        Collections.addAll(list, theNormTokenss);
        
        long endTime = System.currentTimeMillis();
        long time = (endTime - startTime);
        LOG.severe("Time taken for essie tokenizer : " + time );

        
        return list;
    }
    
    public List<String> generateExampleToken(String input)
    {
        List<String> tokens = new LinkedList<>();
        for( String word : input.split(" "))
        {
            if( word.length() > 2 )
            {
                tokens.add(word);
            }
        }
        
        return tokens;
    }
    
     public List<String> generatePhraseToken(String input)
    {
        List<String> tokens = new LinkedList<>();
        String phrase = "";
        for( String word : input.split(" "))
        {
            if( phrase.isEmpty() )
            {
                phrase = word;
            }
            else
            {
                phrase = phrase+" "+word;
                phrase = phrase.trim();
                tokens.add(phrase);   
                phrase = "";
            }

        }
        
        if( !phrase.isEmpty() )
        {
            tokens.add(phrase);  
        }
        
        return tokens;
    }
     
    public static void main( String args[] ) throws Exception
    {
        String query ="John Snow and Modern-Day Environmental Epidemiology\\n\\tDale P. Sandler\\n\\t\\nAm. J. Epidemiol. 2000 152: 1-3.\\n\\n\\t\\n\\t\\n\\t[Extract]\\n\\t\\n\\t[FREE Full Text]\\n\\t\\n\\t&nbsp;";
        //String query = "John Snow and Modern-Day Environmental Epidemiology\n\tDale P. Sandler\n\t\nAm. J. Epidemiol. 2000 152: 1-3.\n\n\t\n\t\n\t[Extract]\n\t\n\t[FREE Full Text]\n\t\n\t&nbsp;";
        String output = query+"-\n" + EssieTokenizerFilter.generateEssieToken( query ).toString();
        System.out.println( output );
    }

}
