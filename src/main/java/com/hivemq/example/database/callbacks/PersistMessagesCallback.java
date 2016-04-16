package com.hivemq.example.database.callbacks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.hivemq.example.etc.DeviceData;
import com.hivemq.spi.callback.CallbackPriority;
import com.hivemq.spi.callback.events.OnPublishReceivedCallback;
import com.hivemq.spi.callback.exception.OnPublishReceivedException;
import com.hivemq.spi.message.PUBLISH;
import com.hivemq.spi.security.ClientData;
import com.hivemq.spi.services.PluginExecutorService;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author Dominik Obermaier
 * @author Florian Limpöck
 */
public class PersistMessagesCallback implements OnPublishReceivedCallback {

    private static Logger log = LoggerFactory.getLogger(PersistMessagesCallback.class);
    private final Provider<Connection> connectionProvider;
    private final PluginExecutorService pluginExecutorService;

    private static final String SQLStatement = "INSERT INTO `data` (deviceId,deviceStatus,deviceCondition,errorCode,topic,qos,client,lastupdated) VALUES (?,?,?,?,?,?,?,?)";
    private static final String MessagesSQLStatement = "INSERT INTO `Messages` (message,topic,qos,client) VALUES (?,?,?,?)";

    @Inject
    public PersistMessagesCallback(final Provider<Connection> connectionProvider,
                                   final PluginExecutorService pluginExecutorService) {
        this.connectionProvider = connectionProvider;
        this.pluginExecutorService = pluginExecutorService;
    }

    @Override
    public void onPublishReceived(final PUBLISH publish, final ClientData clientData) throws OnPublishReceivedException {
        final Connection connection = connectionProvider.get();

        pluginExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement preparedStatement = connection.prepareStatement(SQLStatement);
                    final PreparedStatement publishStatement = connection.prepareStatement(MessagesSQLStatement);
                    preparedStatement.setBytes(1, publish.getPayload());
                    log.info("came here with string payload : "+new String(publish.getPayload()));
                    String payloadString = new String(publish.getPayload());
                    if(payloadString != null && !(payloadString.isEmpty())){
                    	JSONObject jsonObject = new JSONObject(payloadString);
                    	ObjectMapper objectMapper = new ObjectMapper();
                    	DeviceData data = objectMapper.readValue(jsonObject.toString(), DeviceData.class);
                    	publishStatement.setBytes(1, publish.getPayload());
                    	publishStatement.setString(2, publish.getTopic());
                    	publishStatement.setInt(3, publish.getQoS().getQosNumber());
                    	publishStatement.setString(4, clientData.getClientId());
                    	preparedStatement.setString(1, data.getDeviceID());
                    	preparedStatement.setString(2, data.getDeviceStatus());
                    	preparedStatement.setInt(3, data.getDeviceCondition());
                    	preparedStatement.setInt(4, data.getErrorCode());
                    	preparedStatement.setString(5, publish.getTopic());
                        preparedStatement.setInt(6, publish.getQoS().getQosNumber());
                        preparedStatement.setString(7, clientData.getClientId());
                        //get current time and save it to datatbase
                        Date date = new Date();
                        long epochTime = date.getTime();
                        
                        preparedStatement.setLong(8, epochTime);
                        publishStatement.execute();
                        publishStatement.close();
                        preparedStatement.execute();
                        preparedStatement.close();
                    }
                } catch (IOException e){
                	log.error("IO exception occurred with details : " + e.toString());
                } catch (JSONException e){
                	log.error("json exception occurred with details : " + e.toString());
                } catch (SQLException e) {
                    log.error("An error occured while preparing the SQL statement", e);
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        log.error("An error occured while giving back a connection to the connection pool");
                    }
                }
            }
        });

    }

    @Override
    public int priority() {
        return CallbackPriority.HIGH;
    }
}
