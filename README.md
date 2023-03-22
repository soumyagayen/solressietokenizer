# solressietokenizer
Custom Solr Filter which enables us to use Essie Tokenization and Normalization 


Steps to use custom filter.
1.  Place the following jar
    https://github.com/soumyagayen/solressietokenizer/tree/main/solrEssieTokenizer/target/solrEssieTokenizer-1.0-SNAPSHOT.jar
    at the below solr path
    <br>
    <pre>
    /<solr installtion directory>/server/solr/custom-lib/solrEssieTokenizer-1.0-SNAPSHOT.jar
    </pre>
    
2.  Define your solr core.
3.  In your core's solrconfig.xml define the custom library ( Jar file )
      
      <lib path="../custom-lib/customfilter.jar" />
  
4.  Now the custom filter can be used in your solr core. You can use it by defining a custom solr field / custom analyzer for your schema.xml of your core.
    
     <fieldType name="text_custom" class="solr.TextField" positionIncrementGap="100" multiValued="true">
      <analyzer type="index">
        <tokenizer class="solr.KeywordTokenizerFactory"/>
        <filter class="nih.nlm.openi.lucene.customfilter.EssieTokenizerFilterFactory" />
      </analyzer>
      <analyzer type="query">
        <tokenizer class="solr.KeywordTokenizerFactory"/>
        <filter class="nih.nlm.openi.lucene.customfilter.EssieTokenizerFilterFactory" />
      </analyzer>
    </fieldType>  

5.  Now you can use the "text_custom" field , when defining the field type for your data
  
    <-- Example --!>
     <field name="Caption" type="text_custom" indexed="true" stored="true" multiValued="false"/>
    
