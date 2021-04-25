package com.ecm.xmlgen;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"node"})
@XmlRootElement(name = "import")
public class Import {
    protected List<Node> node;
    public List<Node> getNode() {
        if (this.node == null) {
            this.node = new ArrayList<Node>();
        }
        return this.node;
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"location", "file", "title", "category"})
     public static class Node
    {
        @XmlElement(required = true)
     protected String location;
        @XmlElement(required = true)
    protected String file;
        @XmlElement(required = true)
        protected String title;
        @XmlElement(required = true)
        protected Category category;
        @XmlAttribute(name = "type")
        protected String type;
        @XmlAttribute(name = "action")
        protected String action;

        public String getLocation() {
            return this.location;
        }
        public void setLocation(String value) {
            this.location = value;
        }
        public String getFile() {
            return this.file;
        }
        public void setFile(String value) {
            this.file = value;
        }
        public String getTitle() {
            return this.title;
        }
        public void setTitle(String value) {
            this.title = value;
        }
        public Category getCategory() {
            return this.category;
        }
        public void setCategory(Category value) {
            this.category = value;
        }
        public String getType() {
            return this.type;
        }
        public void setType(String value) {
            this.type = value;
        }
        public String getAction() {
            return this.action;
        }
        public void setAction(String value) {
            this.action = value;
        }
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"attribute"})
        public static class Category
        {
        protected List<Attribute> attribute;

            @XmlAttribute(name = "name")
            protected String name;
            public List<Attribute> getAttribute() {
                if (this.attribute == null) {
                    this.attribute = new ArrayList<Attribute>();
                }
                return this.attribute;

            }
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {"value"})
            public static class Attribute
            {
                @XmlValue
            protected String value;
                @XmlAttribute(name = "name")
                protected String name;
                public String getValue() {
                    return this.value;
                }
                public void setValue(String value) {
                    this.value = value;
                }
                public String getName() {
                    return this.name;
                }
                public void setName(String value) {
                    this.name = value;
                }
            }
            public void setName(String propertyValue) {
                this.name = propertyValue;
            }
        }
    }
}