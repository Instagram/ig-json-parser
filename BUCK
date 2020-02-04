load(
    "//tools/build_defs/oss:ig_json_parser_defs.bzl",
    "fb_java_library",
    "fb_native",
)

fb_java_library(
    name = "runtime",
    labels = ["supermodule:android/default/infra.ig_json_parser"],
    tests = ["//fbandroid/libraries/ig-json-parser/processor:processor-test"],
    visibility = [
        "PUBLIC",
    ],
    deps = [
        "//fbandroid/libraries/ig-json-parser/common:common",
    ],
    exported_deps = [
        "//fbandroid/libraries/ig-json-parser/common:common",
        "//fbandroid/third-party/java/jackson:core",
    ],
)

fb_native.java_annotation_processor(
    labels = ["supermodule:android/default/infra.ig_json_parser"],
    name = "processor",
    processor_class = "com.instagram.common.json.annotation.processor.JsonAnnotationProcessor",
    supports_abi_generation_from_source = True,
    visibility = [
        "PUBLIC",
    ],
    deps = [
        "//fbandroid/libraries/ig-json-parser/processor:processor-lib",
    ],
)
