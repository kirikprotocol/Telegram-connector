package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationManager;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.json.*;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jeck on 08/02/16
 */
public class PersonalizationSessionManager implements SessionManager{
    private PersonalizationManager personalization;

    public PersonalizationSessionManager(PersonalizationManager personalization) {
        this.personalization = personalization;
    }

    public Session getSession(String id) throws Exception {
        try {
            String session = personalization.getString(id);
            return string2session(session);
        } catch (Exception e) {
            Session session = new SimpleSession(id);
            this.persist(session);
            return session;
        }
    }

    public void persist(Session session) throws Exception {
        personalization.set(session.getId(), session2string(session));
    }

    private String session2string(Session session) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (String name: session.getAttributesNames()) {
            arrayBuilder.add(Json.createObjectBuilder().add("k", name).add("v", session.getAttribute(name).toString()));
        }
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder().
                add("id", session.getId()).
                add("sd", session.getStartDate().getTime()).
                add("a", arrayBuilder);
        JsonObject json = jsonBuilder.build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            JsonWriter writer = Json.createWriter(os);
            writer.write(json);
            writer.close();
            os.flush();
            return new String(os.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                //todo log
            }
        }
        return "";
    }

    private Session string2session(String s) throws UnsupportedEncodingException {
        InputStream is = new ByteArrayInputStream(s.getBytes("utf-8"));
        try (JsonReader reader = Json.createReader(is)) {
            JsonObject json = reader.readObject();
            SimpleSession session = new SimpleSession();
            session.setId(json.getJsonString("id").getString());
            session.setStartDate(new Date(json.getJsonNumber("sd").longValue()));
            Map<String, Object> attributes = new HashMap<>();
            JsonArray array = json.getJsonArray("a");
            for (int i=0; i<array.size(); i++){
                JsonObject object = array.getJsonObject(i);
                attributes.put(object.getString("k"), object.getString("v"));
            }
            session.setAttributes(attributes);
            return session;
        } finally {
            try{
                is.close();
            } catch (Exception e) {
                //todo log
            }
        }

    }

    public static class Factory implements ResourceFactory {

        @Override
        public PersonalizationSessionManager build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
            return new PersonalizationSessionManager((PersonalizationManager)SADSInitUtils.getResource("personalization", properties));
        }

        @Override
        public boolean isHeavyResource() {
            return false;
        }
    }
}
