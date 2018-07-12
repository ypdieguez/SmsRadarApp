/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tuenti.smsradar;

import android.telephony.SmsManager;

/**
 * Represents a sms stored in Android sms Content Provider.
 * <p/>
 * Address field is the equivalent to the MT/MO author MSISDN in telco terminology.
 * <p/>
 * Review MSISDN standard for more information: http://en.wikipedia.org/wiki/MSISDN
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 * @author Yordan P. Dieguez <ypdieguez@tuta.io>
 */
public class Sms {

    final String date;
    private final String address;
    private final String msg;
    private final SmsType type;


    public Sms(String address, String date, String msg, SmsType type) {
        this.address = address;
        this.date = date;
        this.msg = msg;
        this.type = type;
    }

    SmsType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sms)) return false;

        Sms sms = (Sms) o;

        return
                (address != null ? address.equals(sms.address) : sms.address == null) &&
                        (date != null ? date.equals(sms.date) : sms.date == null) &&
                        (msg != null ? msg.equals(sms.msg) : sms.msg == null) &&
                        type == sms.type;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public int getPages() {
        return SmsManager.getDefault().divideMessage(msg).size() * address.split(",").length;
//        return SmsMessage.calculateLength(msg, false)[0] * address.split(",").length;
    }

    @Override
    public String toString() {
        return "Sms{" +
                "address='" + address + '\'' +
                ", date='" + date + '\'' +
                ", msg='" + msg + '\'' +
                ", type=" + type +
                '}';
    }
}
