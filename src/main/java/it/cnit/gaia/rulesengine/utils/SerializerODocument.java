package it.cnit.gaia.rulesengine.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.List;

@JsonComponent
public class SerializerODocument extends JsonSerializer<List<ODocument>> {

	@Override
	public void serialize(List<ODocument> oDocuments, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeStartArray();
		for(ODocument o : oDocuments){
			jsonGenerator.writeRawValue(o.toJSON("rid,fetchPlan:rule.school:1,dateAsLong"));
		}
		jsonGenerator.writeEndArray();
	}

	/*
	*   {
    "@rid": "#57:7617",
    "timestamp": 1489590105157, 	//Long timestamp
    "rule": {
      "@rid": "#22:17120",
      "in_": [
        "#30:3"
      ],
      "suggestion": "Ciao!",
      "description": "fweffwefw",
      "threshold": 1,
      "name": "Dummy2",
      "school": {					//Expanded School
        "@rid": "#45:25",
        "enabled": true,
        "out_": [
          "#30:2",
          "#30:3"
        ],
        "name": "Gramsci-Keynes",
        "type": "School",
        "aid": 155076,
        "uri": "gaia-prato/gw1/"
      },
      "uri": "Bla",
      "enabled": true
    },
    "values": {
      "suggestion": "Ciao!"
    }
  }
	* */
}
