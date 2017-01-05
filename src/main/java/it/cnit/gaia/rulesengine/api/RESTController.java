package it.cnit.gaia.rulesengine.api;

import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.Fireable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTController {
    Logger LOGGER = Logger.getRootLogger();

    @Autowired
    RulesLoader rulesLoader;

    @RequestMapping("/rules/update")
    public void updateRules(){
        LOGGER.info("Updateing rules...");
        rulesLoader.updateRuleTree("#25:1");
    }

    @RequestMapping("/rules")
    public String test(){
        Fireable f = rulesLoader.getRuleTree("#25:1");
        return f.toString();
    }


}
