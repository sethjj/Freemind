/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database;

/**
 *
 * @author woo
 */
    public class ComboObject {

        private String dateStr;
        private String comment;
        private String version = null;
        private String committer = null;
        private String releaseId = null;
        private String changeRequest = null;

        public ComboObject(String dateStr, String comment) {
            super();
            this.dateStr = dateStr;
            this.comment = comment;
        }

        public ComboObject(String dateStr, String version, String comment) {
            this(dateStr, comment);
            this.version = version;
        }

        public ComboObject(String dateStr, String comment, String committer, String version, String releaseId, String changeRequest) {
            this(dateStr, version, comment);
            this.committer = committer;
            this.releaseId = releaseId;
            this.changeRequest = changeRequest;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(dateStr);
            buffer.append("  |  ");
            if (version != null) {
                buffer.append(version);
                buffer.append("  |  ");
            }
            buffer.append(comment);
            String theString = buffer.toString();
            int length = theString.length();
            return theString.substring(0, Math.min(55, length));
        }

        public String getDateStr() {
            return dateStr;
        }

        public String getComment() {
            return comment;
        }

        public String getVersion() {
            return version;
        }

        public String getCommitter() {
            return committer;
        }

        public String getReleaseId() {
            return releaseId;
        }

        public String getChangeRequest() {
            return changeRequest;
        }

        public String getHistoryString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(toString());
            buffer.append(" | ");
            buffer.append(fixStr(committer, 8));
            buffer.append(" | ");
            buffer.append(fixStr(releaseId, 8));
            buffer.append(" | ");
            buffer.append(fixStr(changeRequest, 8));
            return buffer.toString();
        }

        private String fixStr(String str, int len) {
            String newStr = new String(str + "        ");
            return newStr.substring(0, 8);
        }
    }
