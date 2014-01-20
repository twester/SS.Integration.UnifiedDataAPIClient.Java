package ss.udapi.sdk.examples.model;

import java.util.ArrayList;
import java.util.List;

import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.model.Tag;

public class ModelTestHelper
{
  private Tag tag1 = new Tag();
  private Tag tag2 = new Tag();
  private List<Tag> tags = new ArrayList<Tag>();
  
  private RestLink link1 = new RestLink();
  private RestLink link2 = new RestLink();
  private List<RestLink> Links = new ArrayList<RestLink>();
  
  private Summary summary = new Summary();

  private RestItem restItem1 = new RestItem();
  private List<RestItem> restItems = new ArrayList<RestItem>();
  
  
  public void buildTags()
  {
    tag1.setId(111);
    tag1.setKey("tag1Key");
    tag1.setValue("tag1Value");
    
    tag2.setId(222);
    tag2.setKey("tag2Key");
    tag2.setValue("tag2Value");

    tags.add(tag1);
    tags.add(tag2);
  }

  
  public List<Tag> getTags() 
  {
    return tags;
  }
  
  
  public void buildSummary() 
  {
    buildTags();
    summary.setId("ABC-123");
    summary.setDate("2014-01-14");
    summary.setStartTime("14:00");
    summary.setSequence(1234);
    summary.setTags(tags);
    summary.setMatchStatus(2345);
  }
  
  
  public Summary getSummary() 
  {
    return summary;
  }

  
  public void buildLinks() 
  {
    link1.setRelation("amqp");
    link1.setHref("http://endpoint1");
    String[] verbs1 = {"pause", "unpause"};
    link1.setVerbs(verbs1);
    
    link2.setRelation("http");
    link2.setHref("http://endpoint2");
    String[] verbs2 = {"stop", "start"};
    link2.setVerbs(verbs2);
    
    Links.add(link1);
    Links.add(link2);
  }
  
  
  public List<RestLink> getLinks() 
  {
    return Links;
  }
  
  
  public void buildRestItems()
  {
    buildSummary();
    buildLinks();
    restItem1.setName("Tennis");
    restItem1.setContent(summary);
    restItem1.setLinks(Links);

    restItems.add(restItem1);
  }

  public List<RestItem> getRestItems()
  {
    return restItems;
  }
  
}
