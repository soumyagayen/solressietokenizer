/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nih.nlm.solr.customfilter;

import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 *
 * @author gayens
 */
public class EssieTokenizerFilterFactory extends TokenFilterFactory
{
    public EssieTokenizerFilterFactory( Map<String, String> args )
    {
        super(args);
    }

    @Override
    public TokenStream normalize(TokenStream input)
    {
        return super.normalize(input); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TokenStream create(TokenStream stream)
    {
        return new EssieTokenizerFilter(stream);
    }
    
}
