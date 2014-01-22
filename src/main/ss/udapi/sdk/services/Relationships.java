package ss.udapi.sdk.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

//TODO do we need this?  i don't think so
public final class Relationships
{
  private static Map<String,String> relationNameMap = null;
  private static Logger logger = Logger.getLogger(Relationships.class);

  
  public Relationships()
  {
    throw new UnsupportedOperationException("Constructor not allowed on static class Relationships");
  }
  
  
  private static void initRelations()
  {
    relationNameMap = new HashMap<String,String>();
    relationNameMap.put("http://api.sportingsolutions.com/rels/login", "Login");
    relationNameMap.put("http://api.sportingsolutions.com/rels/usermanagementservice/user/reset", "Reset");
    
  }


  public static String getRelationName(String relationShip)
  {
    String name;
        
    if (relationNameMap == null) {
      initRelations();
    }

    if (relationNameMap.containsKey(relationShip)) {
      name = relationNameMap.get(relationShip);
    } else {
      logger.error("Invalid relationship mapping");
      name = "### Invalid relationship mapping ###";
    }

    return name;
  }
  
  
  
  
}
