-keepnames class com.developersbeeh.medcontrol.data.model.MealAnalysisResult
-if class com.developersbeeh.medcontrol.data.model.MealAnalysisResult
-keep class com.developersbeeh.medcontrol.data.model.MealAnalysisResultJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.developersbeeh.medcontrol.data.model.MealAnalysisResult
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.developersbeeh.medcontrol.data.model.MealAnalysisResult {
    public synthetic <init>(java.lang.String,int,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
