/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package dk.kb.bitrepository.mediator.pillaraccess;

import dk.kb.bitrepository.mediator.pillaraccess.clients.GetFileClient;
import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Factory class for the access module.
 * Instantiates the instances of the interfaces within this module.
 */
public final class AccessPillarFactory {
    /**
     * The singleton instance.
     */
    private static AccessPillarFactory instance;

    /**
     * Instantiation of this singleton.
     *
     * @return The singleton instance of this factory class.
     */
    public static synchronized AccessPillarFactory getInstance() {
        // ensure singleton.
        if (instance == null) {
            instance = new AccessPillarFactory();
        }
        return instance;
    }

    /**
     * Private constructor for initialization of the singleton.
     */
    private AccessPillarFactory() {
    }

    /**
     * Method for getting a GetFileClient.
     *
     * @param settings        The settings for the GetFileClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A GetFileClient.
     */
    public GetFileClient createGetFileClient(Settings settings, SecurityManager securityManager, String clientID) {
        return new GetFileConversation(
                ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager),
                ConversationMediatorManager.getConversationMediator(settings, securityManager),
                settings, clientID);
    }

    /**
     * Method for instantiating a GetChecksumsClient as defined in the access configurations.
     *
     * @param settings        The settings for the GetChecksumsClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return The GetChecksumsClient
     */
    public GetChecksumsClient createGetChecksumsClient(Settings settings, SecurityManager securityManager, String clientID) {
        return null;
    }

    /**
     * Method for getting a GetFileIDsClient as defined in the access configuration.<p>
     *
     * @param settings        The settings for the GetFileIDsClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A GetFileIDsClient.
     */
    public GetFileIDsClient createGetFileIDsClient(Settings settings, SecurityManager securityManager, String clientID) {
        return null;
    }

    /**
     * Method for getting a GetStatusClient as defined in the access configuration.<p>
     *
     * @param settings        The settings for the GetStatusClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A GetStatusClient.
     */
    public GetStatusClient createGetStatusClient(Settings settings, SecurityManager securityManager, String clientID) {
        return null;
    }

    /**
     * Method for getting a AuditTrailClient as defined in the access configuration.<p>
     *
     * @param settings        The settings for the AuditTrailClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A AuditTrailClient.
     */
    public AuditTrailClient createAuditTrailClient(Settings settings, SecurityManager securityManager, String clientID) {
        return null;
    }
}