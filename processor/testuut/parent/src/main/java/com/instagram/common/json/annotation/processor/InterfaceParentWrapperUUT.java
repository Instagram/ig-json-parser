package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class InterfaceParentWrapperUUT {
    @JsonField(fieldName = "implementation1")
    InterfaceImplementationUUT mImplementation1;

    @JsonField(fieldName = "implementation2")
    InterfaceImplementation2UUT mImplementation2;

    public static InterfaceParentWrapperUUT from(InterfaceParentWithWrapperUUT instance) {
        InterfaceParentWrapperUUT wrapper = new InterfaceParentWrapperUUT();

        if (instance instanceof InterfaceImplementationUUT) {
            wrapper.mImplementation1 = (InterfaceImplementationUUT) instance;
        } else if (instance instanceof InterfaceImplementation2UUT) {
            wrapper.mImplementation2 = (InterfaceImplementation2UUT) instance;
        } else {
            throw new IllegalArgumentException("Unknown interface implementation: "
                    + instance.getClass().getName());
        }

        return wrapper;
    }

    public InterfaceParentWithWrapperUUT getInterfaceParentWithWrapperUUT() {
        if (mImplementation1 != null) {
            return mImplementation1;
        } else if (mImplementation2 != null) {
            return mImplementation2;
        } else {
            return null;
        }
    }
}
