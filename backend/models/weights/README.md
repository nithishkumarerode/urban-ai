# UrbanCadastral AI Model Weights Directory

Place your trained segmentation ONNX models here.

Expected default model:
`cadastral_segmentation.onnx`

Classes supported:
0 = Background
1 = Building
2 = Road
3 = Vegetation
4 = Water
5 = Open Land

If this directory does not contain valid weights, the platform will truthfully report:
"AI MODEL NOT AVAILABLE"
and will NOT fabricate fake predictions.
