# solressietokenizer
Custom Solr Filter which enables us to use Essie Tokenization and Normalization 


Steps to use custom filter.
1.  Place the following jar
    https://github.com/soumyagayen/solressietokenizer/tree/main/solrEssieTokenizer/target/solrEssieTokenizer-1.0-SNAPSHOT.jar
    at the below solr path
    <br>
    <p>/&lt;solr install directory&gt;/server/solr/custom-lib/solrEssieTokenizer-1.0-SNAPSHOT.jar</p>


2.  Define your solr core.
3.  In your core's solrconfig.xml define the custom library ( Jar file )
      
      <lib path="../custom-lib/customfilter.jar" />
  
4.  Now the custom filter can be used in your solr core. You can use it by defining a custom solr field / custom analyzer for your schema.xml of your core.
    
 <p>
&nbsp; &nbsp;&lt;fieldType name=&quot;text_custom&quot; class=&quot;solr.TextField&quot; positionIncrementGap=&quot;100&quot; multiValued=&quot;true&quot;&gt;
&nbsp; &nbsp; &nbsp; &lt;analyzer type=&quot;index&quot;&gt;<br />
&nbsp; &nbsp; &nbsp; &nbsp; &lt;tokenizer class=&quot;solr.KeywordTokenizerFactory&quot;/&gt;<br />
&nbsp; &nbsp; &nbsp; &nbsp; &lt;filter class=&quot;nih.nlm.openi.lucene.customfilter.EssieTokenizerFilterFactory&quot; /&gt;<br />
&nbsp; &nbsp; &nbsp; &lt;/analyzer&gt;<br />
&nbsp; &nbsp; &nbsp; &lt;analyzer type=&quot;query&quot;&gt;<br />
&nbsp; &nbsp; &nbsp; &nbsp; &lt;tokenizer class=&quot;solr.KeywordTokenizerFactory&quot;/&gt;<br />
&nbsp; &nbsp; &nbsp; &nbsp; &lt;filter class=&quot;nih.nlm.openi.lucene.customfilter.EssieTokenizerFilterFactory&quot; /&gt;<br />
&nbsp; &nbsp; &nbsp; &lt;/analyzer&gt;<br />
&nbsp; &nbsp; &lt;/fieldType&gt;
</p>  

5.  Now you can use the "text_custom" field , when defining the field type for your data
  
<p>&lt;-- Example --!&gt;<br />
&lt;field name=&quot;Caption&quot; type=&quot;text_custom&quot; indexed=&quot;true&quot; stored=&quot;true&quot; multiValued=&quot;false&quot;/&gt;</p>

    
