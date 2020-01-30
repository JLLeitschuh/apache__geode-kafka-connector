package geode.kafka;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeodeConnectorConfig {

    //GeodeKafka Specific Configuration
    /**
     * Identifier for each task
     */
    public static final String TASK_ID = "GEODE_TASK_ID"; //One config per task
    /**
     * Specifies which Locators to connect to Apache Geode
     */
    public static final String LOCATORS = "locators";
    public static final String DEFAULT_LOCATOR = "localhost[10334]";

    protected final int taskId;
    protected List<LocatorHostPort> locatorHostPorts;

    protected GeodeConnectorConfig() {
        taskId = 0;
    }

    public GeodeConnectorConfig(Map<String, String> connectorProperties) {
        taskId = Integer.parseInt(connectorProperties.get(TASK_ID));
        locatorHostPorts = parseLocators(connectorProperties.get(GeodeConnectorConfig.LOCATORS));
    }


    public static Map<String, List<String>> parseTopicToRegions(String combinedBindings) {
        //It's the same formatting, so parsing is the same going topic to region or region to topic
        return parseRegionToTopics(combinedBindings);
    }

    /**
     * Given a string of the form [region:topic,...] will produce a map where the key is the
     * regionName and the value is a list of topicNames to push values to
     *
     * @param combinedBindings a string with similar form to "[region:topic,...], [region2:topic2,...]
     * @return mapping of regionName to list of topics to update
     */
    public static Map<String, List<String>> parseRegionToTopics(String combinedBindings) {
        if (combinedBindings == "" || combinedBindings == null){
            return null;
        }
        List<String> bindings = parseBindings(combinedBindings);
        return bindings.stream().map(binding -> {
            String[] regionToTopicsArray = parseBinding(binding);
            return regionToTopicsArray;
        }).collect(Collectors.toMap(regionToTopicsArray -> regionToTopicsArray[0], regionToTopicsArray -> parseStringByComma(regionToTopicsArray[1])));
    }

    public static List<String> parseBindings(String bindings) {
        return Arrays.stream(bindings.split("](\\s)*,")).map((s) -> {
            s = s.replaceAll("\\[", "");
            s = s.replaceAll("\\]", "");
            s = s.trim();
            return s;
        }).collect(Collectors.toList());
    }

    private static String[] parseBinding(String binding) {
        return binding.split(":");
    }

    //Used to parse a string of topics or regions
    public static List<String> parseStringByComma(String string) {
        return parseStringBy(string, ",");
    }

    public static List<String> parseStringBy(String string, String regex) {
        return Arrays.stream(string.split(regex)).map((s) -> s.trim()).collect(Collectors.toList());
    }

    public static String reconstructString(Collection<String> strings) {
        return strings.stream().collect(Collectors.joining("],[")) + "]";
    }

    List<LocatorHostPort> parseLocators(String locators) {
        return Arrays.stream(locators.split(",")).map((s) -> {
            String locatorString = s.trim();
            return parseLocator(locatorString);
        }).collect(Collectors.toList());
    }

    private LocatorHostPort parseLocator(String locatorString) {
        String[] splits = locatorString.split("\\[");
        String locator = splits[0];
        int port = Integer.parseInt(splits[1].replace("]", ""));
        return new LocatorHostPort(locator, port);
    }

    public int getTaskId() {
        return taskId;
    }

    public List<LocatorHostPort> getLocatorHostPorts() {
        return locatorHostPorts;
    }
}
