/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.sample.auction.message;

import org.glassfish.tyrus.sample.auction.Auction;
import org.glassfish.tyrus.sample.auction.AuctionItem;

/**
 * @author Stepan Kopriva (stepan.kopriva at oracle.com)
 */
public abstract class AuctionMessage<T> {

    public static final String LOGIN_REQUEST = "lreq";
    public static final String BID_REQUEST = "breq";
    public static final String LOGOUT_REQUEST = "dreq";
    public static final String AUCTION_LIST_REQUEST = "xreq";
    private static final String LOGIN_RESPONSE = "lres";
    private static final String PRICE_UPDATE_RESPONSE = "pres";
    private static final String AUCTION_TIME_RESPONSE = "ares";
    private static final String RESULT_RESPONSE = "rres";
    private static final String AUCTION_LIST_RESPONSE = "xres";

    /*
     * Message type
     */
    private final String type;

    /*
     * Message data
     */
    private final T data;

    /*
     * ID used for communication purposes
     */
    private final String communicationId;

    AuctionMessage(String type, String communicationId, T data) {
        this.type = type;
        this.communicationId = communicationId;
        this.data = data;
    }

    public String asString() {
        return type + Auction.SEPARATOR + communicationId + Auction.SEPARATOR + data.toString();
    }

    public T getData() {
        return data;
    }

    public String getCommunicationId() {
        return communicationId;
    }

    /**
     * @author Stepan Kopriva (stepan.kopriva at oracle.com)
     */
    public static class AuctionListRequestMessage extends AuctionMessage<String> {

        public AuctionListRequestMessage(String communicationId, String message) {
            super(AUCTION_LIST_REQUEST, communicationId, message);
        }
    }

    /**
     * @author Stepan Kopriva (stepan.kopriva at oracle.com)
     */
    public static class AuctionListResponseMessage extends AuctionMessage<String> {

        public AuctionListResponseMessage(String communicationId, String data) {
            super(AUCTION_LIST_RESPONSE, communicationId, data);
        }
    }

    /**
     * @author Stepan Kopriva (stepan.kopriva at oracle.com)
     */
    public static class AuctionTimeBroadcastMessage extends AuctionMessage<Integer> {

        public AuctionTimeBroadcastMessage(String communicationId, int time) {
            super(AUCTION_TIME_RESPONSE, communicationId, time);
        }
    }

    /**
     * @author Stepan Kopriva (stepan.kopriva at oracle.com)
     */
    public static class BidRequestMessage extends AuctionMessage<String> {

        public BidRequestMessage(String communicationId, String data) {
            super(BID_REQUEST, communicationId, data);
        }
    }

    /**
     * @author Stepan Kopriva (stepan.kopriva at oracle.com)
     */
    public static class LoginRequestMessage extends AuctionMessage<String> {

        public LoginRequestMessage(String communicationId, String username) {
            super(LOGIN_REQUEST, communicationId, username);
        }
    }

    public static class LoginResponseMessage extends AuctionMessage<AuctionItem> {

        public LoginResponseMessage(String communicationId, AuctionItem item) {
            super(LOGIN_RESPONSE, communicationId, item);
        }
    }

    /**
     * @author Danny Coward (danny.coward at oracle.com)
     */
    public static class LogoutRequestMessage extends AuctionMessage<String> {

        public LogoutRequestMessage(String communicationId, String username) {
            super(LOGOUT_REQUEST, communicationId, username);
        }
    }

    /**
     * @author Stepan Kopriva (stepan.kopriva at oracle.com)
     */
    public static class PriceUpdateResponseMessage extends AuctionMessage<String> {

        public PriceUpdateResponseMessage(String communicationId, String price) {
            super(PRICE_UPDATE_RESPONSE, communicationId, price);
        }
    }

    /**
     * @author Stepan Kopriva (stepan.kopriva at oracle.com)
     */
    public static class ResultMessage extends AuctionMessage<String> {

        public ResultMessage(String communicationId, String data) {
            super(RESULT_RESPONSE, communicationId, data);
        }
    }
}