package groupchat;

import static constants.Constants.GROUP_CHAT_TOPIC;

public class Group {

    public Group(String groupID, String groupName) {
        this.topic = GROUP_CHAT_TOPIC;
        this.groupID = groupID;
        this.groupName = groupName;
    }

    private final String topic;
    private String groupName;
    private String groupID;

    public String getUniqueIdString() {
        return "group_id_" + groupID;
    }


    public String getTopic() {
        return topic;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }
}
