package io.chub.android.data.api.model;

import java.util.List;


/**
 * Created by guillaume on 11/9/14.
 */
public class GoogleDurationResponse {

    private List<Row> rows;

    public List<Row> getRows() {
        return rows;
    }

    public String getDuration() {
        try {
            return rows.get(0).getElements().get(0).getDuration().getText();
        } catch (Exception ex) {
            return "Eta not available";
        }
    }

    private class Row {

        private List<Element> elements;

        public List<Element> getElements() {
            return elements;
        }

        private class Element {
            private Duration duration;

            public Duration getDuration() {
                return duration;
            }

            private class Duration {
                private String text = "Eta not available";

                public String getText() {
                    return text;
                }
            }
        }
    }
}
