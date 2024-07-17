/*  Copyright (C) 2024 Vitalii Tomin

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */

package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class FileUpload {
    public static final byte id = 0x28;

    public static class FileUploadParams {
        public byte file_id = 0;
        public String protocolVersion = "";
        public short app_wait_time = 0;
        public byte bitmap_enable = 0;
        public short unit_size = 0;
        public int max_apply_data_size = 0;
        public short interval =0;
        public int received_file_size =0;
        public byte no_encrypt = 0;
    }

    public static class Filetype {
        public static final byte watchface = 1;
        public static final byte music = 2;
        public static final byte backgroundImage = 3;
        public static final byte app = 7;
    }


    public static class FileInfoSend {
        public static final byte id = 0x02;
        public static class Request extends HuaweiPacket {

            public Request(ParamsProvider paramsProvider,
                            int fileSize,
                            String fileName,
                            byte fileType) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01, fileName)
                        .put(0x02, fileSize)
                        .put(0x03, (byte) fileType);

                if (fileType == Filetype.watchface) {
                    String watchfaceName = fileName.split("_")[0];
                    String watchfaceVersion = fileName.split("_")[1];
                    this.tlv.put(0x05, watchfaceName)
                            .put(0x06, watchfaceVersion);
                }

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }
        }
    }

    public static class FileHashSend {
        public static final byte id = 0x03;

        public static class Request extends HuaweiPacket {

            public Request(ParamsProvider paramsProvider,
                            byte[] hash,
                            byte fileType) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01, fileType)
                        .put(0x03, hash);
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }
        }
    }

    public static class FileUploadConsultAck {
        public static final byte id = 0x04;
        public static class Request extends HuaweiPacket {
            public Request(ParamsProvider paramsProvider, byte noEncryption, byte fileType) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x7f, 0x000186A0) //ok
                        .put(0x01, fileType);
                if (noEncryption == 1)
                    this.tlv.put(0x09, (byte)0x01); // need on devices which generally encrypted, but files
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {

            public FileUploadParams fileUploadParams = new FileUploadParams();
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }

            @Override
            public void parseTlv() throws HuaweiPacket.ParseException {
                    this.fileUploadParams.file_id = this.tlv.getByte(0x01);
                    this.fileUploadParams.protocolVersion = this.tlv.getString(0x02);
                    this.fileUploadParams.app_wait_time = this.tlv.getShort(0x03);
                    this.fileUploadParams.bitmap_enable = this.tlv.getByte(0x04);
                    this.fileUploadParams.unit_size = this.tlv.getShort(0x05);
                    this.fileUploadParams.max_apply_data_size = this.tlv.getInteger(0x06);
                    this.fileUploadParams.interval = this.tlv.getShort(0x07);
                    this.fileUploadParams.received_file_size = this.tlv.getInteger(0x08);
                    if (this.tlv.contains(0x09)) // optional for older devices
                        this.fileUploadParams.no_encrypt = this.tlv.getByte(0x09);
            }
        }
    }

    public static class FileNextChunkParams extends HuaweiPacket {
        public static final byte id = 0x05;

        public int bytesUploaded = 0;
        public int nextchunkSize = 0;
        public FileNextChunkParams(ParamsProvider paramsProvider) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                this.complete = true;
        }
        @Override
        public void parseTlv() throws HuaweiPacket.ParseException {
            this.bytesUploaded = this.tlv.getInteger(0x02);
            this.nextchunkSize = this.tlv.getInteger(0x03);
        }
    }

    public static class FileNextChunkSend extends HuaweiPacket {
        public static final byte id = 0x06;

        public FileNextChunkSend(ParamsProvider paramsProvider) {
            super(paramsProvider);
            this.serviceId = FileUpload.id;
            this.commandId = id;
            this.complete = true;
        }
    }

    public static class FileUploadResult {
        public static final byte id = 0x07;

        public static class Request extends HuaweiPacket {
            public Request(ParamsProvider paramsProvider, byte fileType) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x7f, 0x000186A0) //ok
                        .put(0x01, fileType);

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            byte status = 0;
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }

            @Override
            public void parseTlv() throws HuaweiPacket.ParseException {
                this.status = this.tlv.getByte(0x02);
            }
        }
    }
}
