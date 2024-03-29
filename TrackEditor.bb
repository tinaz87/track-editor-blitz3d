; -----------------------------------------------------------------------------------
; --- INCLUDES ----------------------------------------------------------------------

Include "3ds.bb"
Include "dgui_1.6\DevilGUI.bb"

; -----------------------------------------------------------------------------------


; --- INIT 3D GRAPHICS --------------------------------------------------------------

; Screen dimensions.
Const ScreenWidth			= 1280
Const ScreenHeight			= 720

Graphics3D ScreenWidth, ScreenHeight, 0, 2
SetBuffer BackBuffer()

; Hide the pointer.
HidePointer()

; -----------------------------------------------------------------------------------


; --- CONST VALUES ------------------------------------------------------------------

; GUI skin.
Const GUISkin$ 					= "MacOS"

; Max number of points allowed.
Const MarkersNumber				= 50

; Max number of auto-generated points.
Const AutoGenPointsNumber 		= 1500

; Mouse turn speed.
Const TurnSpeed#				= 0.8

; Curvature.
Const Curvature#				= 2

; Spline's step.
Const InterpolationStep#		= 5.0

; IA Values.
Const IAAngleDifference#		= 5.0
Const IADistance#				= 10.0

; Marker's selection ray.
Const MarkerSelectionRay#		= 10.0

; Heightmaps size.
Const HeightmapsSize			= 512

; Default terrain's horizontal scale.
Const DefaultTerrainHScale		= 1

; Default terrain's vertical scale.
Const DefaultTerrainVScale		= 50

; Default terrain's tile scale.
Const DefaultTileScale			= 32

; Max number of heightmaps available.
Const MaxHeightmapsNumber		= 20

; Max number of tiles available.
Const MaxTilesNumber 			= 20

; Default heighmtap index.
Const DefaultHeightmapIndex 	= 5

; Default tile index.
Const DefaultTileIndex 			= 6

; Max loadable scene objects.
Const MaxObjectTypes			= 50
Const MaxObjectPerType 			= 20 

; Far away...
Const FarAwayX#					= 0
Const FarAwayY#					= -10000
Const FarAwayZ#					= 0

; Track view mode parameters.
Const CameraInterpolationStep#	= 25.0
Const CameraAngleDifference#	= 5.0
Const CameraCurvature#			= 2
Const CameraDistance#			= 10.0
Const CameraMarkersNumber		= 50
Const AutoGenCameraPointsNumber = 500

; -----------------------------------------------------------------------------------


; --- PATHS -------------------------------------------------------------------------

Const SavedDataPath$	= ".\TrackData\"

Const MediaPath$ 		= ".\Media\"
Const HeightMapsPath$ 	= ".\Media\HeightMaps\"
Const TilesPath$ 		= ".\Media\Tiles\"
Const ObjectsPath$		= ".\Media\3DObjects\"
Const TexturesPath$		= ".\Media\Textures\"
Const IconsPath$		= ".\Media\Icons\"
Const SkyBoxPath$  		= ".\Media\Skydomes\Sunny1\"

; -----------------------------------------------------------------------------------


; --- FILENAMES ---------------------------------------------------------------------

Const TrackData$		= "Track.txt"
Const Track3DS$			= "Track.3ds"
Const HeightmapData$	= "HeigthmapData.txt"
Const ObjectsData$		= "ObjectsData.txt"

Const CameraMarkers$	= "Track.txt"

; -----------------------------------------------------------------------------------


; --- VARIABLES ---------------------------------------------------------------------

Global running 				= True 

; Editor's state.
; 
; Legend:
;			0 = Track's editor
;			1 = Edit the scene
Global editorState			= 0

; Wireframe mode.
Global wire					= 0

; Meshes of the markers.
Dim markers(MarkersNumber)

; Arrays of markers' coordinates.
Dim markersCoordX#(MarkersNumber)
Dim markersCoordY#(MarkersNumber)
Dim markersCoordZ#(MarkersNumber)

; What points have been placed?
Dim markersPlaced(MarkersNumber)

; How many point have been placed?
Global numMarkersPlaced 	= 0

; Current marker's index.
Global currentMarkerIndex 	= -1

; Auto generated markers.
Dim autoGenPoints(AutoGenPointsNumber)

; Auto-generated points.
Dim autoGenPointsCoordX#(AutoGenPointsNumber)
Dim autoGenPointsCoordY#(AutoGenPointsNumber)
Dim autoGenPointsCoordZ#(AutoGenPointsNumber)

; SX e DX points.
Dim autoGenPointsSX(AutoGenPointsNumber)
Dim autoGenPointsDX(AutoGenPointsNumber)

Dim autoGenPointsSXCoord#(AutoGenPointsNumber, 3)
Dim autoGenPointsDXCoord#(AutoGenPointsNumber, 3)

; Angles.
Dim angles#(AutoGenPointsNumber)

; Number of auto-generated points.
Global numAutoGenPoints 	= 0

; Delta of mouse position.
Global mouseDeltaX#			= 0
Global mouseDeltaY#			= 0
; Previous mouse coordinates.
Global mouseOldX			= 0
Global mouseOldY			= 0

; IA values.
Global autoCoordX#			= 0
Global autoCoordZ#			= 0
Global autoAngle#			= 0

Global iaPoint				= 1

; Meshes.
Global trackMesh			= 0
Global trackSurface			= 0

; Cicles counter.
Global ciclesCounter		= 0

; Resources.
Global tex 					= 0

; Plane.
Global plane 				= 0

; Camera.			
Global camera				= 0

; Light.
Global light				= 0

; Skybox.
Global skybox				= 0

; Hey, caught! You've modified the track!
Global updateNeeded			= False

; Do you want to render track's triangles?
Global renderTriangles		= 0

; Terrain.
Global terrain				= 0
; Terrain's tile.
Global terrainTile			= 0

; Heighmaps
Dim heightmaps(MaxHeightmapsNumber)	
Dim heightmapsPaths$(MaxHeightmapsNumber)

; Selected heightmap.
Global selectedHeightmap	= DefaultHeightmapIndex

; Tiles.
Dim tiles(MaxTilesNumber)
Dim tilesPaths$(MaxTilesNumber)

; Selected tile.
Global selectedTile			= DefaultTileIndex

; Terrain scales.
Global terrainHScale#		= DefaultTerrainHScale
Global terrainVScale#		= DefaultTerrainVScale

; Scene objects.
Dim objects(MaxObjectTypes, MaxObjectPerType)

; Objects placed.
Dim objectsPlaced(MaxObjectTypes)

; Objects properties.
Dim objectsPositionsX#(MaxObjectTypes, MaxObjectPerType)
Dim objectsPositionsY#(MaxObjectTypes, MaxObjectPerType)
Dim objectsPositionsZ#(MaxObjectTypes, MaxObjectPerType)

Dim objectsScaleX#(MaxObjectTypes, MaxObjectPerType)
Dim objectsScaleY#(MaxObjectTypes, MaxObjectPerType)
Dim objectsScaleZ#(MaxObjectTypes, MaxObjectPerType)

Dim objectsRotationY#(MaxObjectTypes, MaxObjectPerType)

; Object is placed.
Global objectIsPlaced 		= False

; Placing coordinates.
Global objectPlacingX#		= 0
Global objectPlacingY#		= 0
Global objectPlacingZ#		= 0

; Selected type.
Global selectedType			= 0

; Selected object.
;Global selectedObject		= 0

; If you're adding a new object to the scene, say 'true'.
Global addObject 			= 0

; Current global scale factor (for selected scene object).
Global scale# 				= 1
; Do you have enabled the proportional scale mode?
Global scaleIsChecked 		= 0

; -----------------------------------------------------------------------------------


; --- TRACK VIEW --------------------------------------------------------------------

Global oldCameraCoordX#			= 0
Global oldCameraCoordY#			= 0
Global oldCameraCoordZ#			= 0

Global oldCameraPitch#			= 0
Global oldCameraYaw#			= 0
Global oldCameraRoll#			= 0

Global numCameraPointsPlaced 	= 0

Dim cameraPointsPlaced(MarkersNumber)

Dim cameraPointsCoordX#(MarkersNumber)
Dim cameraPointsCoordY#(MarkersNumber)
Dim cameraPointsCoordZ#(MarkersNumber)

Dim autoGenCameraPoints(AutoGenPointsNumber)

Dim autoGenCameraPointsCoordX#(AutoGenPointsNumber)
Dim autoGenCameraPointsCoordY#(AutoGenPointsNumber)
Dim autoGenCameraPointsCoordZ#(AutoGenPointsNumber)

Global cameraPoint 				= 1
Global numAutoGenCameraPoints 	= 0

Global cameraCoordX# 			= 0
Global cameraCoordZ# 			= 0

Global cameraAngle# 			= 0

Global cameraCiclesCounter 		= 0

Global prevoiousCameraPoint 	= 0
Global cameraPointTarget 		= 1

Const ViewSpeed# 				= 0.5

Global trackViewMode 			= 0 

; -----------------------------------------------------------------------------------


; --- DEVIL GUI'S VARIABLES ---------------------------------------------------------

Global comWin

; Menu buttons.
Global btnNew
Global btnLoad
Global btnSave
Global btnSwitch
Global btnExit

; Track group.
Global lblCurrentMarkerIndex
Global lblNumMarkersPlaced

; Terrain group.
Global lstHeightmaps
Global imgHeightmap
Global lstTiles
Global imgTile

Global lblHorizontalScaleFactor
Global spnHorizontalScaleFactor
Global lblVerticalScaleFactor
Global spnVerticalScaleFactor

Global btnLoadTerrain

; Scene group.
Global lst3DObjects
Global btnAddObject

; Object properties group.
Global lblObjectScale, sldObjectScale, chkObjectScale, lblObjectScaleValue
Global lblObjectXScale, sldObjectXScale, lblObjectXScaleValue
Global lblObjectYScale, sldObjectYScale, lblObjectYScaleValue
Global lblObjectZScale, sldObjectZScale, lblObjectZScaleValue
Global lblObjectRotation, sldObjectRotation, lblObjectRotationValue

; -----------------------------------------------------------------------------------


; --- INIT --------------------------------------------------------------------------

; Init the GUI.
InitGUI()

; Create the window.
CreateWindow()

; Load resources.
LoadResources()

; Create a skydome.
InitSkybox()

; Init the markers' meshes.
InitMarkers()

; Init the auto-generated markers' meshes.
InitAutoGenPoints()

; Create the plane.
InitGround()

; Init the terrain.
InitTerrain()

; Create the camera.
InitCamera()

; Create and init the light.
InitLight()

; -----------------------------------------------------------------------------------


; --- MAIN LOOP ---------------------------------------------------------------------

; Main function.
MainLoop()

; -----------------------------------------------------------------------------------


; --- FUNCTIONS ---------------------------------------------------------------------

; Main loop.
Function MainLoop()

	While (running = True)
		
		Cls 
		
		If (editorState = 0)
			
			; Track's editor.
			TrackEditorMode()
			
		Else
			
			; Edit the scene.
			SceneEditorMode()
			
		EndIf 
		
		; Render the world.
		RenderWorld()
		
		GUI_UpdateGUI()
		
		UpdateWindow()
		
		Flip 
		
	Wend 
	
	GUI_FreeGUI()

End Function

; ### TRACK'S EDITOR ####################################################
Function TrackEditorMode()
	
	; Enter in wireframe mode by pressing 'R'.
	If KeyHit(19) Then wire = 1 - wire
	WireFrame wire
	
	If KeyHit(20)
		
		renderTriangles = 1 - renderTriangles
		
		updateNeeded = True
		
	EndIf 
	
	; Check if you've pressed CTRL (SX).
	If (KeyDown(29) = True)
		
		AddMarker()
		
	; Check if you've pressed SHIFT (SX).
	ElseIf (KeyDown(42) = True)
		
		MoveMarker()
		
	Else
		
		MoveCamera()
		
	EndIf 
	
	; Increment (or decrement) current point index.
	;mouseWheelStatus = MouseZSpeed()
	
	;If (mouseWheelStatus = 1)
	
	;	currentMarkerIndex = currentMarkerIndex + 1
	
	;EndIf
	
	;If (mouseWheelStatus = -1)
	
	;	currentMarkerIndex = currentMarkerIndex - 1	
	
	;EndIf
	
	; Clamp the index value.
	;If (currentMarkerIndex < 0)
	
	;	currentMarkerIndex = 0  
	
	;EndIf
	
	;If (currentMarkerIndex > MarkersNumber - 1)
	
	;	currentMarkerIndex = MarkersNumber - 1
	
	;EndIf 
	
	If (KeyDown(46) = True)
		
		ResetCameraPosition()
		
	EndIf 
	
	; Update.
	TrackEditorUpdate()
	
End Function

; ### EDIT THE SCENE ####################################################
Function SceneEditorMode()
	
	If (trackViewMode = 1)
		
		; Track view mode.
		ViewTrack()
		
	Else 
		
		PositionObject()
		
		UpdateObjects()
		
		If (KeyDown(46) = True)
			
			ResetCameraPosition()
			
		EndIf
		
	EndIf 
	
	; Enter in 'Track View Mode'.
	If (KeyHit(47) = True)
		
		; Enable...
		If (trackViewMode = 0)
			
			; Save camera's statuts.
			SaveCameraState()
			
			; Load markers data from file.
			LoadCameraPoints()
			
			; Init the path.
			InitTrackView()
			
			; Go!
			trackViewMode = 1
			
		Else
			
			; Uninit path.
			QuitTrackView()
			
			; ...or disable it.
			trackViewMode = 0
			
		EndIf
		
	EndIf 
	
	; Camera manager.
	MoveCamera()
	
End Function

; Load resources.
Function LoadResources()
	
	DebugLog("Loading textures set...")
	
	; Textures set.
	tex = LoadTexture(TexturesPath$ + "base_0.png")
	
	DebugLog("Done.")
	
	DebugLog("Loading 3D objects...")
	
	LoadObjects()
	
	DebugLog("Done.")
	
End Function 

; Create a terrain using a specified heightmap.
Function CreateTerrain()
	
	DebugLog("Creating a new terrain...")
	
	; Create the terrain.
	terrain = LoadTerrain(heightmapsPaths$(selectedHeightmap))
	
	; Scale it.
	ScaleEntity terrain, terrainHScale, terrainVScale, terrainHScale
	
	; Position it.
	horizontalOffset = HeightmapsSize / 2 * terrainHScale
	PositionEntity terrain, -horizontalOffset, 0, -horizontalOffset
	
	; Texturize it.
	terrainTile = LoadTexture(tilesPaths$(selectedTile))
	ScaleTexture terrainTile, DefaultTileScale, DefaultTileScale
	EntityTexture terrain, terrainTile
	
	EntityPickMode terrain, 2
	
	DebugLog("Done.")
	
End Function

; Destroy the terrain.
Function DestroyTerrain()
	
	DebugLog("Destroying current terrain...")
	
	If (terrain > 0)
		
		FreeEntity terrain
		terrain = 0
		
	EndIf 
	
;	If (terrainTile > 0)
;		
;		FreeEntity terrainTile
;		terrainTile = 0
;		
;	EndIf 
	
	DebugLog("Done.")
	
End Function

; Destroy the current terrain and create a new one.
Function LoadNewTerrain()
	
	;DebugLog("Loading terrain...")
	
	DestroyTerrain()
	
	CreateTerrain()
	
	;DebugLog("Done.")
	
End Function 
	
; Create the plane.
Function InitGround()
	
	DebugLog("Init ground...")
	
	;plane = CreateCube()
	;ScaleEntity plane, 1000, 1, 1000
	;EntityPickMode plane, 2  
	
	plane = CreatePlane()
	PositionEntity plane, 0, -1, 0
	EntityPickMode plane, 2
	
	DebugLog("Done.")
	
End Function 

; Init the terrain.
Function InitTerrain()
	
	CreateTerrain()
	
End Function

; Reset camera position.
Function ResetCameraPosition()
	
	PositionEntity camera, 0, 500, 0
	PointEntity camera, plane
	
End Function

; Create a camera.
Function InitCamera()
	
	DebugLog("Init camera...")
	
	camera = CreateCamera()
	
	ResetCameraPosition()
	
	DebugLog("Done.")
	
End Function 

; Create and init the light.
Function InitLight()
	
	DebugLog("Init lights...")
	
	light = CreateLight()
	LightColor light, 100, 100, 100
	TurnEntity light, 90, 0, 0
	
	DebugLog("Done.")
	
End Function

; Create a skydome.
Function InitSkybox()
	
	DebugLog("Init skydbox...")
	
	;	skydome = CreateSphere() 
	;	ScaleEntity skydome, 2000, 2000, 2000 
	;	FlipMesh skydome 
	;	EntityFX skydome, 1 
	;	skydomeTexture = LoadTexture("sky.jpg") 
	;	EntityTexture skydome, skydomeTexture 
	
	skybox = CreateMesh()
	; Front face.
	b = LoadBrush(SkyBoxPath$ + "Sunny1_front.png", 49)
	s = CreateSurface(skybox, b)
	AddVertex s, -1, +1, -1, 0, 0 : AddVertex s, +1, +1, -1, 1, 0
	AddVertex s, +1, -1, -1, 1, 1 : AddVertex s, -1, -1, -1, 0, 1
	AddTriangle s, 0, 1, 2 : AddTriangle s, 0, 2, 3
	FreeBrush b
	; Left face.
	b = LoadBrush(SkyBoxPath$ + "Sunny1_left.png", 49)
	s = CreateSurface(skybox, b)
	AddVertex s, +1, +1, -1, 0, 0 : AddVertex s, +1, +1, +1, 1, 0
	AddVertex s, +1, -1, +1, 1, 1 : AddVertex s, +1, -1, -1, 0, 1
	AddTriangle s, 0, 1, 2 : AddTriangle s, 0, 2, 3
	FreeBrush b
	; Back face.
	b = LoadBrush(SkyBoxPath$ + "Sunny1_back.png", 49)
	s = CreateSurface(skybox, b)
	AddVertex s, +1, +1, +1, 0, 0 : AddVertex s, -1, +1, +1, 1, 0
	AddVertex s, -1, -1, +1, 1, 1 : AddVertex s, +1, -1, +1, 0, 1
	AddTriangle s, 0, 1, 2 : AddTriangle s, 0, 2, 3
	FreeBrush b
	; Right face.
	b = LoadBrush(SkyBoxPath$ + "Sunny1_right.png", 49)
	s = CreateSurface(skybox, b)
	AddVertex s, -1, +1, +1, 0, 0 : AddVertex s, -1, +1, -1, 1, 0
	AddVertex s, -1, -1, -1, 1, 1 : AddVertex s, -1, -1, +1, 0, 1
	AddTriangle s, 0, 1, 2 : AddTriangle s, 0, 2, 3
	FreeBrush b
	; Top face.
	b = LoadBrush(SkyBoxPath$ + "Sunny1_up.png", 49)
	s = CreateSurface(skybox, b)
	AddVertex s, -1, +1, +1, 0, 1 : AddVertex s, +1, +1, +1, 0, 0
	AddVertex s, +1, +1, -1, 1, 0 : AddVertex s, -1, +1, -1, 1, 1
	AddTriangle s, 0, 1, 2 : AddTriangle s, 0, 2, 3
	FreeBrush b
	; Bottom face.
	b = LoadBrush(SkyBoxPath$ + "Sunny1_down.png", 49)
	s = CreateSurface(skybox, b)
	AddVertex s, -1, -1, -1, 1, 0 : AddVertex s, +1, -1, -1, 1, 1
	AddVertex s, +1, -1, +1, 0, 1 : AddVertex s, -1, -1, +1, 0, 0
	AddTriangle s, 0, 1, 2 : AddTriangle s, 0, 2, 3
	FreeBrush b
	
	ScaleMesh skybox, 100, 100, 100
	FlipMesh skybox
	EntityFX skybox, 1
	EntityOrder skybox, 10
	
	DebugLog("Done.")
	
End Function

; Reset markers position.
Function ResetMarkersPosition()
	
	DebugLog("Resetting markers position...")
	
	For n = 0 To MarkersNumber - 1
		
		; Put the marker away.
		PositionEntity markers(n), FarAwayX#, FarAwayY#, FarAwayZ#
		
	Next
	
	DebugLog("Done.")
	
End Function 

; Init markers' meshes.
Function InitMarkers()
	
	DebugLog("Init markers...")
	
	For n = 0 To MarkersNumber - 1
		
		markersPlaced(n) = False
		
		markers(n) = CreateSphere(2)
		EntityColor markers(n), 60, 60, 60
		ScaleEntity markers(n), 5, 5, 5
		
	Next
	
	DebugLog("Done.")
	
	ResetMarkersPosition()
	
End Function

; Init auto-generated points' meshes.
Function InitAutoGenPoints()
	
	DebugLog("Init auto-generated points...")
	
	; Main points.
	For n = 0 To AutoGenPointsNumber - 1
		
		autoGenPoints(n) = CreateSphere(2)
		EntityColor autoGenPoints(n), 80, 80, 80
		ScaleEntity autoGenPoints(n), 2, 2, 2
		
		; Put the marker away.
		PositionEntity autoGenPoints(n), FarAwayX#, FarAwayY#, FarAwayZ#
		
	Next
	
	; SX and DX points.
	For n = 0 To AutoGenPointsNumber - 1
		
		autoGenPointsSX(n) = CreateSphere(2)
		EntityColor autoGenPointsSX(n), 0, 0, 0
		ScaleEntity autoGenPointsSX(n), 2, 2, 2
		
		autoGenPointsDX(n) = CreateSphere(2)
		EntityColor autoGenPointsDX(n), 255, 255, 255
		ScaleEntity autoGenPointsDX(n), 1, 1, 1
		
		; Put the marker away.
		PositionEntity autoGenPointsSX(n), FarAwayX#, FarAwayY#, FarAwayZ#
		PositionEntity autoGenPointsDX(n), FarAwayX#, FarAwayY#, FarAwayZ#
		
	Next
	
	DebugLog("Done.")
	
End Function 

; Destroy markers.
Function DestroyMarkers()
	
	DebugLog("Destroy markers...")
	
	For n = 0 To MarkersNumber - 1
		
		FreeEntity markers(n)
		markers(n) = 0
		
	Next
	
	DebugLog("Done.")
	
End Function

; Destroy auto-gen. points.
Function DestroyAutoGenPoints()
	
	DebugLog("Destroy auto-generated points...")
	
	For n = 0 To AutoGenPointsNumber - 1
		
		FreeEntity autoGenPoints(n)
		
		autoGenPoints(n) = 0
		
		FreeEntity autoGenPointsSX(n)
		FreeEntity autoGenPointsDX(n)
		
		autoGenPointsSX(n) = 0
		autoGenPointsDX(n) = 0
		
	Next
	
	DebugLog("Done.")
	
End Function 

; Camera management.
Function MoveCamera()
	
	If (trackViewMode = 0)
		
		xx = KeyDown(205) - KeyDown(203)
		zz = KeyDown(200) - KeyDown(208)
		
		MoveEntity(camera, xx, 0, zz)
		
		x# = EntityX(camera) 
		y# = EntityY(camera) 
		z# = EntityZ(camera) 
		
		terrainLevel# = TerrainY(terrain, x#, y#, z#) + 5 
		
		If (y# < terrainLevel#)
			
			PositionEntity camera, x#, terrainLevel#, z#
			
		EndIf
		
	EndIf 
		
	; Check if you've pressed mouse DX.
	If MouseDown(2)
		
		mouseDeltaX# = MouseX() - mouseOldX
		mouseDeltaY# = MouseY() - mouseOldY
		
		TurnEntity(camera, mouseDeltaY# * TurnSpeed#, 0, 0, False)
		TurnEntity(camera, 0, mouseDeltaX# * -TurnSpeed#, 0, True)
		
	EndIf
	
	PositionEntity skybox, EntityX(camera), EntityY(camera), EntityZ(camera)
	
	mouseOldX = MouseX()
	mouseOldY = MouseY()
	
End Function

; Move a selected marker.
Function MoveMarker()
	
	; Pick a specific marker.
	If (MouseHit(1) = True)
	
		currentPick = CameraPick(camera, MouseX(), MouseY())
	
		If (currentPick > 0)
		
			px# = PickedX#()
			py# = PickedY#()
			pz# = PickedZ#()
		
			; Check if there's a marker near to the picked point.
			For n = 0 To MarkersNumber - 1
			
				mx# = markersCoordX#(n)
				my# = markersCoordY#(n)
				mz# = markersCoordZ#(n)
			
				d = Distance#(px#, py#, pz#, mx#, my#, mz#) 
			
				If (d < MarkerSelectionRay#)
					
					currentMarkerIndex = n
				
				EndIf 
			
			Next
		
		EndIf 
		
	ElseIf (MouseDown(2) = True)
		
		currentPick = CameraPick(camera, MouseX(), MouseY())
		
		If (currentPick > 0)
			
			px# = PickedX#()
			py# = PickedY#()
			pz# = PickedZ#()
			
			mx# = markersCoordX#(currentMarkerIndex)
			my# = markersCoordY#(currentMarkerIndex)
			mz# = markersCoordZ#(currentMarkerIndex)
			
			If (px# <> mx# Or py# <> my# Or pz# <> mz#)
				
				markersCoordX#(currentMarkerIndex) = px#
				markersCoordY#(currentMarkerIndex) = py#
				markersCoordZ#(currentMarkerIndex) = pz#
				
				updateNeeded = True
				
			EndIf 
			
		EndIf 
		
	EndIf 
	
End Function 

; Add a marker.
Function AddMarker()
	
	If (MouseHit(1))
		
		currentPick = CameraPick(camera, MouseX(), MouseY())  
		
		; Check if you've picked the plane.
		If (currentPick > 0)
			
			If (currentMarkerIndex <> MarkersNumber - 1)
				
				currentMarkerIndex = currentMarkerIndex + 1
				
				numMarkersPlaced = numMarkersPlaced + 1
				
			EndIf 
			
			px# = PickedX#()
			py# = PickedY#()
			pz# = PickedZ#()
			
			markersCoordX#(currentMarkerIndex) = px#
			markersCoordY#(currentMarkerIndex) = py#
			markersCoordZ#(currentMarkerIndex) = pz#
			
			PositionEntity markers(currentMarkerIndex), markersCoordX#(currentMarkerIndex), markersCoordY#(currentMarkerIndex), markersCoordZ#(currentMarkerIndex)
			
			markersPlaced(currentMarkerIndex) = True
			
			DebugLog("New marker (" + currentMarkerIndex + ") added in " + px# + " " + py# + " " + pz# + ".")
			
			updateNeeded = True
			
		EndIf
		
	EndIf 
	
End Function

; Reset markers.
Function ResetMarkersData()
	
	DebugLog("Resetting markers data...")
	
	numMarkersPlaced 	= 0
	
	currentMarkerIndex 	= -1
	
	For n = 0 To MarkersNumber - 1
		
		markersCoordX#(n) = 0
		markersCoordY#(n) = 0
		markersCoordZ#(n) = 0
		
	Next
	
	DebugLog("Done.")
	
End Function
	
; Reset values.
Function ResetValues()
	
	iaPoint 			= 1
	
	numAutoGenPoints 	= 0
	
	autoCoordX# = markersCoordX#(0)
	autoCoordZ# = markersCoordZ#(0)
	
	xx# = markersCoordX#(0) - markersCoordX#(1)
	zz# = markersCoordZ#(0) - markersCoordZ#(1)
	
	autoAngle# = 270 - ATan2#(xx#, zz#)
	
	ciclesCounter		= 0
	
End Function 

; Reset the position of the points.
Function ResetPoints()
	
	DebugLog("Resetting points...")
	
	; Main points.
	For n = 0 To AutoGenPointsNumber - 1
		
		
		; Put the marker away.
		PositionEntity autoGenPoints(n), FarAwayX#, FarAwayY#, FarAwayZ#
		
		; Put the marker away.
		PositionEntity autoGenPointsSX(n), FarAwayX#, FarAwayY#, FarAwayZ#
		PositionEntity autoGenPointsDX(n), FarAwayX#, FarAwayY#, FarAwayZ#
		
	Next
	
	DebugLog("Done.")
	
End Function

; Create track's mesh.
Function CreateTrack()
	
	DestroyTrack()
	
	CreateTrackMesh()
	
End Function 

; Create track's mesh.
Function CreateTrackMesh()
	
	DebugLog("Creating track's mesh...")
	
	; Create a new mesh.
	trackMesh = CreateMesh()
	trackSurface = CreateSurface(trackMesh)
	
	EntityPickMode trackMesh, 2
	
	; Add vertexes and triangles to the mesh.
	For n = 0 To numAutoGenPoints - 1
		
		surface = trackSurface
		
		If (n = numAutoGenPoints - 1)
			
			v0 = AddVertex(surface, autoGenPointsSXCoord#(n, 0), autoGenPointsSXCoord#(n, 1) + 2, autoGenPointsSXCoord#(n, 2), 0, 0)
			v1 = AddVertex(surface, autoGenPointsSXCoord#(0, 0), autoGenPointsSXCoord#(0, 1) + 2, autoGenPointsSXCoord#(0, 2), .25, 0)
			
			v2 = AddVertex(surface, autoGenPointsDXCoord#(n, 0), autoGenPointsDXCoord#(n, 1) + 2, autoGenPointsDXCoord#(n, 2), .25, .25)
			v3 = AddVertex(surface, autoGenPointsDXCoord#(0, 0), autoGenPointsDXCoord#(0, 1) + 2, autoGenPointsDXCoord#(0, 2), 0, .25)
			
		Else 
			
			v0 = AddVertex(surface, autoGenPointsSXCoord#(n, 0), autoGenPointsSXCoord#(n, 1) + 2, autoGenPointsSXCoord#(n, 2), 0, 0)
			v1 = AddVertex(surface, autoGenPointsSXCoord#(n + 1, 0), autoGenPointsSXCoord#(n + 1, 1) + 2, autoGenPointsSXCoord#(n + 1, 2), .25, 0)
			
			v2 = AddVertex(surface, autoGenPointsDXCoord#(n, 0), autoGenPointsDXCoord#(n, 1) + 2, autoGenPointsDXCoord#(n, 2), .25, .25)
			v3 = AddVertex(surface, autoGenPointsDXCoord#(n + 1, 0), autoGenPointsDXCoord#(n + 1, 1) + 2, autoGenPointsDXCoord#(n + 1, 2), 0, .25)
			
		EndIf 
		
		AddTriangle(surface, v0, v1, v2)
		AddTriangle(surface, v1, v3, v2)
		
	Next
	
	EntityColor trackMesh, 80, 80, 80
	EntityTexture trackMesh, tex
	
	DebugLog("Done.")
	
End Function

; Destroy track's mesh.
Function DestroyTrack()
	
	DebugLog("Destroying track's mesh...")
	
	; Clean an existing mesh.
	If (trackMesh > 0)
		
		FreeEntity trackMesh
		trackMesh = 0
		
	End If 
	
	DebugLog("Done.")
	
End Function 

; Reset terrain.
Function ResetTerrain()
	
	DebugLog("Resetting terrain properties...")
	
	selectedHeightmap = DefaultHeightmapIndex
	selectedTile = DefaultTileIndex
	
	terrainHScale = DefaultTerrainHScale
	terrainVScale = DefaultTerrainVScale
	
	DebugLog("Done.")
	
End Function

; Update function ('You don't say?!' XD).
Function TrackEditorUpdate()
	
	If (updateNeeded = True And numMarkersPlaced <> 0)
		
		DebugLog("Updating track...")
		
		;--------------------------------------------------------------------------------------------
		
		; Reset values.
		ResetValues()
		
		;--------------------------------------------------------------------------------------------
		
		DebugLog("Setting markers position...")
		
		For n = 0 To numMarkersPlaced - 1
			
			PositionEntity markers(n), markersCoordX#(n), markersCoordY#(n), markersCoordZ#(n)
			
			If (n = currentMarkerIndex)
				EntityColor markers(n), 0, 255, 0
			Else
				EntityColor markers(n), 255, 0, 0
			EndIf 
			
		Next
		
		;--------------------------------------------------------------------------------------------
		
		DebugLog("Generating spline's main points...")
		
		While (iaPoint <= numMarkersPlaced + 1)
			
			currentPoint = iaPoint Mod numMarkersPlaced
			
			xx# = autoCoordX# - markersCoordX#(currentPoint)
			zz# = autoCoordZ# - markersCoordZ#(currentPoint)
			
			d# = Sqr#(xx# * xx# + zz# * zz#)
			angle# = 270 - ATan2#(xx#, zz#)
			difference# = AngleDifference#(autoAngle#, angle#)
			
			If (difference# < -IAAngleDifference#)
				autoAngle# = autoAngle# - Curvature#
			EndIf 
			
			If (difference# > IAAngleDifference#)
				autoAngle# = autoAngle# + Curvature#
			EndIf 
			
			If (Abs(difference#) < IAAngleDifference#)
				autoAngle# = angle#
			EndIf 
			
			If (d# > IADistance#)
				
				autoCoordX# = autoCoordX# + Cos#(autoAngle#)
				autoCoordZ# = autoCoordZ# + Sin#(autoAngle#)
				
				ciclesCounter = ciclesCounter + 1
				
				If (iaPoint > 1 And ciclesCounter > InterpolationStep#)
					
					ciclesCounter = 0
					
					autoGenPointsCoordX#(numAutoGenPoints) = autoCoordX#
					autoGenPointsCoordZ#(numAutoGenPoints) = autoCoordZ#
					
					; The y-coordinate is calculalted sampling the terrain.
					y# = TerrainY(terrain, autoCoordX#, 0, autoCoordZ#)
					autoGenPointsCoordY#(numAutoGenPoints) = y#
					
					; PositionEntity 	autoGenPoints(numAutoGenPoints), 
					;					autoGenPointsCoordX#(numAutoGenPoints), 
					;					0, 
					;					autoGenPointsCoordZ#(numAutoGenPoints)
					
					numAutoGenPoints = numAutoGenPoints + 1 
					
					If (numAutoGenPoints > AutoGenPointsNumber - 1)
						
						numAutoGenPoints = AutoGenPointsNumber - 1
						
					End If 
					
				EndIf 
				
			Else
				
				iaPoint = iaPoint + 1
				
			End If 
			
		Wend
		
		DebugLog(numAutoGenPoints + " points generated.")
		
		;--------------------------------------------------------------------------------------------
		
		DebugLog("Generating lateral points...")
		
		; Calc. sx and dx points.
		For n = 0 To numAutoGenPoints - 1
			
			xx# = 0
			yy# = 0
			
			If (n = numAutoGenPoints - 1)
				
				xx# = autoGenPointsCoordX#(n) - autoGenPointsCoordX#(0)
				zz# = autoGenPointsCoordZ#(n) - autoGenPointsCoordZ#(0)
				
			Else 
			
				xx# = autoGenPointsCoordX#(n) - autoGenPointsCoordX#(n + 1)
				zz# = autoGenPointsCoordZ#(n) - autoGenPointsCoordZ#(n + 1)
				
			EndIf 
				
			angles#(n) = 270 - ATan2#(xx#, zz#)
			
			; Set SX point.
			
			xx# = autoGenPointsCoordX#(n) + Cos#(angles#(n) + 90) * 10
			zz# = autoGenPointsCoordZ#(n) + Sin#(angles#(n) + 90) * 10
			
			autoGenPointsSXCoord#(n, 0) = xx#
			autoGenPointsSXCoord#(n, 2) = zz#
			
			; The y-coordinate is calculalted sampling the terrain.
			yy# = TerrainY(terrain, xx#, 0, zz#)
			autoGenPointsSXCoord#(n, 1) = yy#
			
			xx# = autoGenPointsCoordX#(n) + Cos#(angles#(n) - 90) * 10
			zz# = autoGenPointsCoordZ#(n) + Sin#(angles#(n) - 90) * 10		
			
			; Set DX point.
			autoGenPointsDXCoord#(n, 0) = xx#
			autoGenPointsDXCoord#(n, 2) = zz#
			
			; The y-coordinate is calculalted sampling the terrain.
			yy# = TerrainY(terrain, xx#, 0, zz#)
			autoGenPointsDXCoord#(n, 1) = yy#
			
		Next
		
		;--------------------------------------------------------------------------------------------
		
		; Reset the position of the points.
		ResetPoints()
		
		If (renderTriangles = 1)
			
			CreateTrack()
			
		Else 
			
			DestroyTrack()
			
			DebugLog("Setting points position...")
			
			For n = 0 To numAutoGenPoints - 1
				
				; Position main points.
				x# = autoGenPointsCoordX#(n)
				z# = autoGenPointsCoordZ#(n)
				
				y# = TerrainY(terrain, x#, 0, z#)
				
				PositionEntity autoGenPoints(n), x#, y#, z#
				
				; Position SX and DX points.
				sx# = autoGenPointsSXCoord#(n, 0)
				sy# = autoGenPointsSXCoord#(n, 1)
				sz# = autoGenPointsSXCoord#(n, 2)
				
				PositionEntity autoGenPointsSX(n), sx#, sy#, sz#
				
				dx# = autoGenPointsDXCoord#(n, 0)
				dy# = autoGenPointsDXCoord#(n, 1)
				dz# = autoGenPointsDXCoord#(n, 2)
				
				PositionEntity autoGenPointsDX(n), dx#, dy#, dz#
				
			Next	
			
		EndIf 
			
		;--------------------------------------------------------------------------------------------
		
		updateNeeded = False 
		
		DebugLog("Track's update complete.")
		
	EndIf 
	
End Function 

; -----------------------------------------------------------------------------------

; Save the current track to file.
Function SaveMarkersData()
	
	DebugLog("Saving markers data...")
	
	outFile = WriteFile(SavedDataPath$ + TrackData$)
	
	For n = 0 To MarkersNumber - 1
		
		WriteByte(outFile, markersPlaced(n))
		
		WriteFloat(outFile, markersCoordX#(n))
		WriteFloat(outFile, markersCoordY#(n))
		WriteFloat(outFile, markersCoordZ#(n))
		
	Next
	
	CloseFile outFile
	
	DebugLog("Done.")
	
End Function 

; Load a track from file.
Function LoadMarkersData()
	
	DebugLog("Loading markers data...")
	
	loadedPoints = 0
	
	If (FileType(SavedDataPath$ + TrackData$))
		
		inFile = ReadFile(SavedDataPath$ + TrackData$)
		
		For n = 0 To MarkersNumber - 1
			
			markersPlaced(n) = ReadByte(inFile)
			
			markersCoordX#(n) = ReadFloat(inFile)
			markersCoordY#(n) = ReadFloat(inFile)
			markersCoordZ#(n) = ReadFloat(inFile)
			
			If (markersPlaced(n) = True)
				
				loadedPoints = loadedPoints + 1
				
			End If 
			
		Next
		
		numMarkersPlaced = loadedPoints
		
		currentMarkerIndex = loadedPoints - 1
		
		CloseFile inFile
		
		; Reset values.
		ResetValues()
		
		; Track needs to be updated.
		updateNeeded = True
		
	EndIf
	
	DebugLog("Done (" + loadedPoints + " points loaded).")
	
End Function

; Save the track as a 3DS object.
Function Save3DS()
	
	DebugLog("Saving 3DS mesh...")
	
	SaveMesh3DS(trackMesh, SavedDataPath$ + Track3DS$) 
	
	DebugLog("Done.")
	
End Function 

; Create the track and save it as a 3DS object.
Function CreateAndSave3DS()
	
	CreateTrack()
	
	Save3DS()
	
	DestroyTrack()
	
End Function

; Save the track (markers and 3DS).
Function SaveTrack()
	
	SaveMarkersData()
	
	Save3DS()
	
End Function

; Save scene objects data.
Function SaveObjectsData()
	
	DebugLog("Saving objects data...")
	
	file = WriteFile(SavedDataPath$ + ObjectsData$)
	
	; How many objects I must save?
	nrObject = 0
	
	For n = 0 To MaxObjectTypes - 1
		
		nrObject = nrObject + objectsPlaced(n)
		
	Next
	
	; Write the file!
	WriteInt(file, nrObject)
	
	For n = 0 To MaxObjectTypes - 1
		
		For k = 0 To objectsPlaced(n) - 1
			
			WriteInt(file, n)
			WriteFloat(file, objectsPositionsX#(n, k))
			WriteFloat(file, objectsPositionsY#(n, k))
			WriteFloat(file, objectsPositionsZ#(n, k)) 
			WriteFloat(file, objectsRotationY#(n, k))
			WriteFloat(file, objectsScaleX#(n, k))
			WriteFloat(file, objectsScaleY#(n, k))
			WriteFloat(file, objectsScaleZ#(n, k))
			
		Next
		
	Next
	
	CloseFile file
	
	DebugLog("Done (" + nrObject + " objects saved).")
	
End Function

; Load scene objects data.
Function LoadObjectsData()
	
	DebugLog("Loading objects data...")
	
	Local nrObjects = 0
	
	If (FileType(SavedDataPath$ + ObjectsData$))
		
		file = ReadFile(SavedDataPath$ + ObjectsData$)
		
		; How many objects have you saved?
		nrObjects = ReadInt(file)
		
		; Read!
		For i = 0 To nrObjects - 1
			
			n = ReadInt(file) 
			
			k = objectsPlaced(n)
			
			currentObject = objects(n, k)
			
			objectsPositionsX#(n, k) = ReadFloat(file)
			objectsPositionsY#(n, k) = ReadFloat(file) 
			objectsPositionsZ#(n, k) = ReadFloat(file) 
			
			objectsRotationY#(n, k) = ReadFloat(file) 
			
			objectsScaleX#(n, k) =  ReadFloat(file) 
			objectsScaleY#(n, k) =  ReadFloat(file) 
			objectsScaleZ#(n, k) =  ReadFloat(file)
			
			PositionEntity currentObject, objectsPositionsX#(n, k),objectsPositionsY#(n, k),objectsPositionsZ#(n, k)
			
			ScaleEntity currentObject,objectsScaleX#(n, k),objectsScaleY#(n, k),objectsScaleZ#(n, k)
			
			RotateEntity currentObject, 0, objectsRotationY(n, k), 0
			
			objectsPlaced(n) = objectsPlaced(n) + 1
			
		Next
		
		CloseFile file
		
	EndIf
	
	DebugLog("Done (" + nrObjects + " objects loaded).")
	
End Function

; Save current heightmap.
Function SaveTerrainData()
	
	DebugLog("Saving terrain data (" + selectedHeightmap + ")...")
	
	file = WriteFile(SavedDataPath$ + HeightmapData$)
	
	; Heightmap index.
	WriteInt(file, selectedHeightmap)
	; Tile index.
	WriteInt(file, selectedTile)
	
	; Scale factors.
	WriteFloat(file, terrainHScale)
	WriteFloat(file, terrainVScale)
	
	CloseFile file
	
	DebugLog("Done.")
	
End Function

; Load an heightmap.
Function LoadTerrainData()
	
	DebugLog("Loading terrain data...")
	
	If (FileType(SavedDataPath$ + HeightmapData$))
	
		file = ReadFile(SavedDataPath$ + HeightmapData$)
		
		; Heightmap index.
		selectedHeightmap = ReadInt(file)
		; Tile index.
		selectedTile = ReadInt(file)
		
		; Scale factors.
		terrainHScale = ReadFloat(file)
		terrainVScale = ReadFloat(file)
		
		CloseFile file
	
	EndIf
	
	DebugLog("Done.")
	
End Function

; Load track's 3DS mesh.
Function Load3DSTrack()
	
	trackMesh = LoadMesh(SavedDataPath$ + Track3DS$)
	EntityPickMode trackMesh, 2
	
End Function 

; -----------------------------------------------------------------------------------

; Useful function in order to create s curvature.
Function AngleDifference#(angle1#, angle2#)
	
	Return ((angle2 - angle1) Mod 360 + 540) Mod 360 - 180
	
End Function

; Distance beteween two points.
Function Distance#(p1X#, p1Y#, p1Z#, p2X#, p2Y#, p2Z#)
	
	px# = (p1X# - p2X#) * (p1X# - p2X#)
	py# = (p1Y# - p2Y#) * (p1Y# - p2Y#)
	pz# = (p1Z# - p2Z#) * (p1Z# - p2Z#)
	
	Return (px# + py# + pz#)
	
End Function

; Clamp function.
Function Clamp(val#, min#, max#)
	
	
	If (val# < min#)
		Return min#
	Else
		If (val# > max#)
			Return max#
		Else
			Return val#
		EndIf
	EndIf
	
	
End Function

; -----------------------------------------------------------------------------------


; --- EDITOR ------------------------------------------------------------------------

; Reset objects position
Function ResetObjectsPosition()
	
	; Position the objects away and copy them.
	For n = 0 To MaxObjectTypes - 1
		
		If (objects(n, 0) > 0)
			
			PositionEntity objects(n, 0), FarAwayX#, FarAwayY#, FarAwayZ#
			
			For k = 1 To MaxObjectPerType - 1
				
				objects(n, k) = CopyEntity(objects(n, 0))
				
			Next 
			
		EndIf
	Next
	
End Function 

; Load objects.
Function LoadObjects()
	
	index = 0
	dir = ReadDir(ObjectsPath$)
	
	Repeat
		
		f$ = NextFile(dir)
		If f$ = "" Then Exit
		ext$ = Right(f$, 3)
		If ((FileType(ObjectsPath$ + f$) = 1) And (f$ <> ".") And (f$ <> "..") And (Lower$(ext$) = "3ds" Or Lower$(ext$) = "b3d")) Then
			
			filename$ = Left(f$, Len(f$) - 4)
			GUI_Message(lst3DObjects, "additem", -1, filename$)
			file$ = ObjectsPath$ + f$
			objects(index, 0) = LoadMesh(file$)
			
			index = index + 1
			
		EndIf
		
	Forever
	
	CloseDir dir
	
	; Position the objects away and copy them.
	For n = 0 To MaxObjectTypes - 1
		
		If (objects(n, 0) > 0)
			
			PositionEntity objects(n, 0), FarAwayX#, FarAwayY#, FarAwayZ#
			
			For k = 1 To MaxObjectPerType - 1
				
				objects(n, k) = CopyEntity(objects(n, 0))
				
			Next 
			
		EndIf
		
	Next
	
End Function

; Load all heightmap textures.
Function LoadHeightmaps()
	
	index = 0
	dir = ReadDir(HeightMapsPath$)
	
	Repeat
		
		f$ = NextFile(dir)
		If f$ = "" Then Exit
		If ((FileType(HeightMapsPath$ + f$) = 1) And (f$ <> ".") And (f$ <> "..")) Then
			filename$ = Left(f$, Len(f$) - 4)
			GUI_Message(lstHeightmaps, "additem", -1, filename$)
			file$ = HeightMapsPath$ + f$
			heightmaps(index) = LoadTexture(file$)
			heightmapsPaths$(index) = file$
			index = index + 1
		EndIf
		
	Forever
	
	CloseDir dir
	
	
End Function

;Load all terrain's tiles.
Function LoadTiles()
	
	index = 0
	dir = ReadDir(TilesPath$)
	
	Repeat
		
		f$ = NextFile(dir)
		If f$ = "" Then Exit
		If ((FileType(TilesPath$ + f$) = 1) And (f$ <> ".") And (f$ <> "..")) Then
			filename$ = Left(f$, Len(f$) - 4)
			GUI_Message(lstTiles, "additem", -1, filename$)
			file$ = TilesPath$ + f$
			tiles(index) = LoadTexture(file$)
			tilesPaths$(index) = file$
			index = index + 1
		EndIf
		
	Forever
	
	CloseDir dir
	
End Function

; Update objects.
Function UpdateObjects()
	
	; Nothing to do! LOL!
	
End Function

; Position objects.
Function PositionObject()
	
	If (addObject = 1)
		
		currentPick = CameraPick(camera, MouseX(), MouseY())
		
		If (currentPick > 0)
			
			n = selectedType
			k = objectsPlaced(selectedType)
			
			obj = objects(n, k)
			
			If (objectIsPlaced = False)
				
				objectPlacingX# = PickedX#()
				objectPlacingY# = PickedY#()
				objectPlacingZ# = PickedZ#()
				
				ScaleEntity obj, objectsScaleX#(n, k), objectsScaleY#(n, k), objectsScaleZ#(n, k)
				
				PositionEntity obj, objectPlacingX#, objectPlacingY#, objectPlacingZ#
				
				RotateEntity obj, 0, objectsRotationY#(n, k), 0
				
				objectIsPlaced = MouseDown(3)
				
			EndIf
			
			If (KeyDown(14)) ; BackSpace
				
				objectIsPlaced = False
				
			EndIf
			
			If (objectIsPlaced) ; Mouse Wheel Button
				
				objectsPositionsX#(n, k) = objectPlacingX#
				objectsPositionsY#(n, k) = objectPlacingY#
				objectsPositionsZ#(n, k) = objectPlacingZ#
				
				ScaleEntity obj, objectsScaleX#(n, k), objectsScaleY#(n, k), objectsScaleZ#(n, k)
				
				RotateEntity obj, 0, objectsRotationY#(n, k), 0
				
			EndIf
			
			If ((KeyDown(28)) And (objectIsPlaced)) ; Enter is KeyDown
				
				objectIsPlaced = False
				addObject = 0
				
				objectsPlaced(selectedType) = objectsPlaced(selectedType) + 1
				
				GUI_Message(btnAddObject, "setenabled", True)
				
			EndIf
			
			If ((KeyDown(1)) And (objectIsPlaced)) ; Esc is KeyDown
				
				objectIsPlaced = False
				addObject = 0
				
				ResetObject(n,k)
				
				GUI_Message(btnAddObject, "setenabled", True)
				
			EndIf
			
		EndIf 
		
	EndIf
	
End Function

; Reset a single object (position, scale and orientation).
Function ResetObject(n, k)
	
	If (objects(n , k) > 0)
	
		ScaleEntity objects(n, k), 1, 1, 1
		
		PositionEntity objects(n, k), FarAwayX#, FarAwayY#, FarAwayZ#
		
		RotateEntity objects(n, k), 0, 0, 0
		
	EndIf 
	
End Function 

; Reset objects state.
Function ResetObjects()
	
;	For n = 0 To MaxObjectTypes - 1 
;		
;		For k = 0 To MaxObjectPerType - 1
;			
;			FreeEntity objects(n, k)
;			
;		Next
;		
;	Next
;	
;	For n = 0 To MaxObjectTypes - 1
;		
;		objectsPlaced(n) = 0
;		
;	Next 
;	
;	; Clean objects listbox.
;	GUI_Message(lst3DObjects, "clear")
;	
;	; Reload objects and position them away.
;	LoadObjects()
	
	For n = 0 To MaxObjectTypes - 1 
		
		For k = 0 To MaxObjectPerType - 1
			
			ResetObject(n, k)
			
		Next
		
	Next
	
	For n = 0 To MaxObjectTypes - 1
		
		objectsPlaced(n) = 0
		
	Next 
		
End Function

; Save current camera state.
Function SaveCameraState()
	
	oldCameraCoordX# = EntityX#(camera)
	oldCameraCoordY# = EntityY#(camera)
	oldCameraCoordZ# = EntityZ#(camera)
	
	oldCameraPitch# = EntityPitch#(camera)
	oldCameraYaw# = EntityYaw#(camera)
	oldCameraRoll# = EntityRoll#(camera)
	
End Function

; Load camera's markers.
Function LoadCameraPoints()
	
	DebugLog("Loading camera points...")
	
	loadedPoints = 0
	
	If (FileType(SavedDataPath$ + CameraMarkers$))
		
		inFile = ReadFile(SavedDataPath$ + CameraMarkers$)
		
		For n = 0 To CameraMarkersNumber - 1
			
			cameraPointsPlaced(n) = ReadByte(inFile)
			
			cameraPointsCoordX#(n) = ReadFloat(inFile)
			cameraPointsCoordY#(n) = ReadFloat(inFile)
			cameraPointsCoordZ#(n) = ReadFloat(inFile)
			
			If (cameraPointsPlaced(n) = True)
				
				loadedPoints = loadedPoints + 1
				
			End If 
			
		Next
		
		numCameraPointsPlaced = loadedPoints
		
		CloseFile inFile
		
	EndIf
	
	DebugLog("Done (" + loadedPoints + " points loaded).")
	
End Function

; Init track view mode.
Function InitTrackView()
	
	DebugLog("Init camera values...")
	
	cameraPoint 			= 1
	
	numAutoGenCameraPoints 	= 0
	
	cameraCoordX# = cameraPointsCoordX#(0)
	cameraCoordZ# = cameraPointsCoordZ#(0)
	
	xx# = cameraPointsCoordX#(0) - cameraPointsCoordX#(1)
	zz# = cameraPointsCoordZ#(0) - cameraPointsCoordZ#(1)
	
	cameraAngle# = 270 - ATan2#(xx#, zz#)
	
	cameraCiclesCounter = 0
	
	prevoiousCameraPoint = 0
	cameraPointTarget = 1
	
	DebugLog("Calculating auto-generated points...")
	
	markerColor = 0
	
	While (cameraPoint <= numCameraPointsPlaced + 1)
		
		currentPoint = cameraPoint Mod numCameraPointsPlaced
		
		xx# = cameraCoordX# - cameraPointsCoordX#(currentPoint)
		zz# = cameraCoordZ# - cameraPointsCoordZ#(currentPoint)
		
		d# = Sqr#(xx# * xx# + zz# * zz#)
		angle# = 270 - ATan2#(xx#, zz#)
		difference# = AngleDifference#(cameraAngle#, angle#)
		
		If (difference# < -CameraAngleDifference#)
			cameraAngle# = cameraAngle# - CameraCurvature#
		EndIf 
		
		If (difference# > CameraAngleDifference#)
			cameraAngle# = cameraAngle# + CameraCurvature#
		EndIf 
		
		If (Abs(difference#) < CameraAngleDifference#)
			cameraAngle# = angle#
		EndIf 
		
		If (d# > CameraDistance#)
			
			cameraCoordX# = cameraCoordX# + Cos#(cameraAngle#)
			cameraCoordZ# = cameraCoordZ# + Sin#(cameraAngle#)
			
			cameraCiclesCounter = cameraCiclesCounter + 1
			
			If (cameraPoint > 1 And cameraCiclesCounter > CameraInterpolationStep#)
				
				cameraCiclesCounter = 0
				
				autoGenCameraPointsCoordX#(numAutoGenCameraPoints) = cameraCoordX#
				autoGenCameraPointsCoordZ#(numAutoGenCameraPoints) = cameraCoordZ#
				
				; The y-coordinate is calculated sampling the terrain.
				y# = TerrainY(terrain, cameraCoordX#, 0, cameraCoordZ#)
				autoGenCameraPointsCoordY#(numAutoGenCameraPoints) = y#
				
				autoGenCameraPoints(numAutoGenCameraPoints) = CreateSphere(2)
				ScaleEntity autoGenCameraPoints(numAutoGenCameraPoints), 5, 5, 5
				
				EntityColor autoGenCameraPoints(numAutoGenCameraPoints), markerColor, markerColor, markerColor
				markerColor = markerColor + 1
				If (markerColor > 255) Then markerColor = 255
				
				PositionEntity 	autoGenCameraPoints(numAutoGenCameraPoints), autoGenCameraPointsCoordX#(numAutoGenCameraPoints), autoGenCameraPointsCoordY#(numAutoGenCameraPoints) + 10, autoGenCameraPointsCoordZ#(numAutoGenCameraPoints)
				
				HideEntity autoGenCameraPoints(numAutoGenCameraPoints)
				
				numAutoGenCameraPoints = numAutoGenCameraPoints + 1 
				
				If (numAutoGenCameraPoints > AutoGenCameraPointsNumber - 1)
					
					numAutoGenCameraPoints = AutoGenCameraPointsNumber - 1
					
				End If 
				
			EndIf 
			
		Else
			
			cameraPoint = cameraPoint + 1
			
		End If 
		
	Wend
	
	DebugLog("Positioning camera...")
	
	PositionEntity camera, autoGenCameraPointsCoordX#(0), autoGenCameraPointsCoordY#(0) + 10, autoGenCameraPointsCoordZ#(0)
	PointEntity camera, autoGenCameraPoints(1)
	
	DebugLog("Camera target: " + 1 + "/" + (numAutoGenCameraPoints - 1) + ".")
	
	PositionEntity skybox, EntityX(camera), EntityY(camera), EntityZ(camera)
	
End Function

; Track view mode cicle.
Function ViewTrack()
		
	; DebugLog("Camera target: " + cameraPointTarget + ".")
		
	; Movement's direction.
	xx# = autoGenCameraPointsCoordX#(cameraPointTarget) - autoGenCameraPointsCoordX#(prevoiousCameraPoint)
	yy# = autoGenCameraPointsCoordY#(cameraPointTarget) - autoGenCameraPointsCoordY#(prevoiousCameraPoint)
	zz# = autoGenCameraPointsCoordZ#(cameraPointTarget) - autoGenCameraPointsCoordZ#(prevoiousCameraPoint)
		
	norm# = Sqr#(xx# * xx# + yy# * yy# + zz# * zz#) 
		
	; Movement vector.
	xx# = (xx# / norm#) * ViewSpeed#
	yy# = (yy# / norm#) * ViewSpeed#
	zz# = (zz# / norm#) * ViewSpeed#
		
	; Set camera position (And skybox).
	cx# = EntityX#(camera)
	cy# = EntityY#(camera)
	cz# = EntityZ#(camera)
		
	PositionEntity camera, cx# + xx#, cy# + yy#, cz# + zz#
	PositionEntity skybox, EntityX(camera), EntityY(camera), EntityZ(camera)
		
	; Get New camera position.
	cx# = EntityX#(camera)
	cy# = EntityY#(camera)
	cz# = EntityZ#(camera)
		
	; Did you have reached the target?
	dx# = autoGenCameraPointsCoordX#(cameraPointTarget) - cx#
	dy# = autoGenCameraPointsCoordY#(cameraPointTarget) - cy#
	dz# = autoGenCameraPointsCoordZ#(cameraPointTarget) - cz#
		
	d# = Sqr#(dx# * dx# + dz# * dz#)
		
	If (d# < ViewSpeed# * 2)
			
		prevoiousCameraPoint = (cameraPointTarget) Mod (numAutoGenCameraPoints)
		cameraPointTarget = (cameraPointTarget + 1) Mod (numAutoGenCameraPoints)
			
		DebugLog("Camera target: " + cameraPointTarget + "/" + (numAutoGenCameraPoints - 1) + ".")
			
	EndIf 
	
End Function 

; Exit from track view mode.
Function QuitTrackView()
	
	; Reset track view parameters.
	numCameraPointsPlaced 	= 0
	
	cameraPoint 			= 1
	numAutoGenCameraPoints 	= 0
	
	cameraCoordX# 			= 0
	cameraCoordZ# 			= 0
	
	cameraAngle# 			= 0
	
	cameraCiclesCounter 	= 0
	
	prevoiousCameraPoint 	= 0
	cameraPointTarget 		= 1
	
	; Destroy all camera's markers.
	For n = 0 To CameraMarkersNumber - 1
		
		cameraPointsPlaced(n) = False
		
		cameraPointsCoordX#(n) = FarAwayX#
		cameraPointsCoordY#(n) = FarAwayY#
		cameraPointsCoordZ#(n) = FarAwayZ#
		
	Next
	
	For n = 0 To AutoGenCameraPointsNumber - 1
		
		FreeEntity autoGenCameraPoints(n)
		
		autoGenCameraPointsCoordX#(n) = FarAwayX#
		autoGenCameraPointsCoordY#(n) = FarAwayY#
		autoGenCameraPointsCoordZ#(n) = FarAwayZ#
		
	Next 
	
	; Set the previous camera position.
	RotateEntity camera, oldCameraPitch#, oldCameraYaw#, oldCameraRoll#
	PositionEntity camera, oldCameraCoordX#, oldCameraCoordY#, oldCameraCoordZ#
	
End Function

; -----------------------------------------------------------------------------------


; --- DEVIL GUI ---------------------------------------------------------------------

Function InitGUI()
	
	GUI_InitGUI("dgui_1.6\Skins\" + GUISkin$ + ".skin")
	
End Function

Function SetGuiState(state)
	
	If (state = 0)
		
		; Track editor.
		GUI_Message(comWin, "settext", "Track editor")
		
		; Enable:
		GUI_Message(spnHorizontalScaleFactor, "setenabled", True)
		GUI_Message(spnVerticalScaleFactor, "setenabled", True)
		GUI_Message(btnLoadTerrain, "setenabled", True)
		
		; Disable:
		GUI_Message(btnAddObject, "setenabled", False)
		GUI_Message(sldObjectScale, "setenabled", False)
		GUI_Message(chkObjectScale, "setenabled", False)
		GUI_Message(sldObjectXScale, "setenabled", False)
		GUI_Message(sldObjectYScale, "setenabled", False)
		GUI_Message(sldObjectZScale, "setenabled", False)
		GUI_Message(sldObjectRotation, "setenabled", False)
		
	Else 
		
		; Scene editor.
		GUI_Message(comWin, "settext", "Scene editor")
		
		; Disable:
		GUI_Message(spnHorizontalScaleFactor, "setenabled", False)
		GUI_Message(spnVerticalScaleFactor, "setenabled", False)
		GUI_Message(btnLoadTerrain, "setenabled", False)
		
		; Enable:
		GUI_Message(btnAddObject, "setenabled", True)
		GUI_Message(sldObjectScale, "setenabled", True)
		GUI_Message(chkObjectScale, "setenabled", True)
		GUI_Message(sldObjectXScale, "setenabled", True)
		GUI_Message(sldObjectYScale, "setenabled", True)
		GUI_Message(sldObjectZScale, "setenabled", True)
		GUI_Message(sldObjectRotation, "setenabled", True)
		
	EndIf
	
End Function

Function SetTerrainGUI()
	
	GUI_Message(lstHeightmaps, "setvalue", selectedHeightmap)
	GUI_Message(lstTiles, "setvalue", selectedTile)
	
	GUI_Message(spnHorizontalScaleFactor, "setvalue", terrainHScale)
	GUI_Message(spnVerticalScaleFactor, "setvalue", terrainVScale)
	
End Function

Function CreateWindow()
	
	comWin = GUI_CreateWindow(ScreenWidth - 280, 0, 280, ScreenHeight, "", "", False, False, True, False)
	GUI_Message(comWin, "setLocked", True)
	
	; MENU
	grpMenu = GUI_CreateGroupBox(comWin, 10, 5, 270, 50, "Menu")
	
	btnNew = GUI_CreateButton(grpMenu, 15, 20, 45, 20,"", IconsPath$ + "new.png")
	btnLoad = GUI_CreateButton(grpMenu, 65, 20, 45, 20,"", IconsPath$ + "open.png")
	btnSave = GUI_CreateButton(grpMenu, 115, 20, 45, 20,"", IconsPath$ + "save.png")
	btnSwitch = GUI_CreateButton(grpMenu, 165, 20, 45, 20, "", IconsPath$ + "switch.png")
	btnExit = GUI_CreateButton(grpMenu, 215, 20, 45, 20, "", IconsPath$ + "exit.png")
	
	; TRACK
	grpTrack = GUI_CreateGroupBox(comWin, 10, 55, 270, 55, "Track")
	
	lblCurrentMarkerIndex = GUI_CreateLabel(grpTrack, 25, 15, "Current marker index: " + currentMarkerIndex)
	lblNumMarkersPlaced = GUI_CreateLabel(grpTrack, 25, 30, "Marker placed: " + numMarkersPlaced)
	
	; TERRAIN
	grpTerrain = GUI_CreateGroupBox(comWin, 10, 110, 270, 310, "Terrain")
	
	; Heightmaps.
	lstHeightmaps = GUI_CreateListBox(grpTerrain, 25, 20, 80, 100)
	GUI_Message(lstHeightmaps, "setselected", selectedHeightmap)
	
	LoadHeightmaps()
	
	imgHeightmap = GUI_CreateImage(grpTerrain, 125, 20, 100, 100, heightmapsPaths$(selectedHeightmap))
	
	; Tiles.	
	lstTiles = GUI_CreateListBox(grpTerrain, 25, 130, 80, 100)
	GUI_Message(lstTiles, "setselected", selectedTile)
	
	LoadTiles()
	
	imgTile = GUI_CreateImage(grpTerrain, 125, 130, 100, 100, tilesPaths$(selectedTile))
	
	; Scale factors.
	lblHorizontalScaleFactor = GUI_CreateLabel(grpTerrain, 25, 245, "H:")
	spnHorizontalScaleFactor = GUI_CreateSpinner(grpTerrain, 50, 240, 75, DefaultTerrainHScale, 1, 100)
	
	lblVerticalScaleFactor = GUI_CreateLabel(grpTerrain, 130, 245, "V:")
	spnVerticalScaleFactor = GUI_CreateSpinner(grpTerrain, 150, 240, 75, DefaultTerrainVScale, 1, 100)
	
	; Load a specified terrain.
	btnLoadTerrain = GUI_CreateButton(grpTerrain, 25, 270, 205, 25, "Load Terrain")
	
	; 3D OBJECTS - SCENE
	
	grpScene = GUI_CreateGroupBox(comWin, 10, 420, 270, 120, "Scene")
	
	lst3DObjects = GUI_CreateListBox(grpScene, 25, 20, 200, 60)
	GUI_Message(lst3DObjects, "setselected", selectedObject)
	
	btnAddObject = GUI_CreateButton(grpScene, 25, 85, 200, 25, "Add object")
	
	; 3D OBJECTS - PROPERTIES
	
	grpObjectProperties = GUI_CreateGroupBox(comWin, 10, 540, 270, 150, "Object Properties")
	
	
	lblObjectScale = GUI_CreateLabel(grpObjectProperties, 25, 20, "Scale")
	sldObjectScale = GUI_CreateSlider(grpObjectProperties, 75, 25, 120, 1, 0.01, 10)
	lblObjectScaleValue = GUI_CreateLabel(grpObjectProperties, 200, 20, "1.0x")
	chkObjectScale = GUI_CreateCheckBox(grpObjectProperties, 230, 20, "")
	
	GUI_Message(sldObjectScale, "setvalue", scale#)
	
	
	lblObjectXScale = GUI_CreateLabel(grpObjectProperties, 25, 45, "X Scale")
	sldObjectXScale = GUI_CreateSlider(grpObjectProperties, 75, 50, 120, 1, 0.01, 10)
	lblObjectXScaleValue = GUI_CreateLabel(grpObjectProperties, 200, 45, "1.0x")
	
	lblObjectYScale = GUI_CreateLabel(grpObjectProperties, 25, 70, "Y Scale")
	sldObjectYScale = GUI_CreateSlider(grpObjectProperties, 75, 75, 120, 1, 0.01, 10)
	lblObjectYScaleValue = GUI_CreateLabel(grpObjectProperties, 200, 70, "1.0x")
	
	lblObjectZScale = GUI_CreateLabel(grpObjectProperties, 25, 95, "Z Scale")
	sldObjectZScale = GUI_CreateSlider(grpObjectProperties, 75, 100, 120, 1, 0.01, 10)
	lblObjectZScaleValue = GUI_CreateLabel(grpObjectProperties, 200, 95, "1.0x")
	
	lblObjectRotation = GUI_CreateLabel(grpObjectProperties, 25, 120, "Rotation")
	sldObjectRotation = GUI_CreateSlider(grpObjectProperties, 75, 125, 120, 0, 0, 360)
	lblObjectRotationValue = GUI_CreateLabel(grpObjectProperties, 200, 120, "0�")
	
	; Set GUI state.
	SetGuiState(editorState)
	
End Function

Function ResetSliders()
	
	GUI_Message(sldObjectScale, "setvalue", 1)
	GUI_Message(sldObjectXScale, "setvalue", 1)
	GUI_Message(sldObjectYScale, "setvalue", 1)
	GUI_Message(sldObjectZScale, "setvalue", 1)
	GUI_Message(sldObjectRotation, "setvalue", 0)
	
End Function

Function UpdateWindow()
	
	; MENU
	
	; New.
	If (GUI_AppEvent() = btnNew)
		
		If (editorState = 0)
			
			; New track.
			
			DestroyTrack()
			
			; Reset markers.
			ResetMarkersData()
			ResetMarkersPosition()
			
			ResetPoints()
			
			; Reset values.
			ResetValues()
			
			; Reset terrain...
			ResetTerrain()
			SetTerrainGUI()
			; ...and reload it.
			LoadNewTerrain()
			
			; Update the track.
			; updateNeeded = True
			
		Else 
			
			; New scene.
			ResetObjects()
			
		EndIf 
		
	EndIf
	
	; Load
	If (GUI_AppEvent() = btnLoad)
		
		If (editorState = 0)
			
			; Load track from file.
			LoadMarkersData()
			
			LoadTerrainData()
			
			SetTerrainGUI()
			
			LoadNewTerrain()
			
		Else 
			
			; Reset object (put them away).
			ResetObjects()
			
			; Load scene from file.
			LoadObjectsData()
			
		EndIf 
		
	EndIf
	
	; Save.
	If (GUI_AppEvent() = btnSave)
		
		If (editorState = 0)
			
			; Save track.
			SaveMarkersData()
			
			SaveTerrainData()
			
		Else 
			
			; Save scene.
			SaveObjectsData()
			
		EndIf
		
	EndIf 
	
	; Switch editor mode.
	If (GUI_AppEvent() = btnSwitch)
		
		If (editorState = 0)
			
			; Switch to scene editor.
			
			SaveMarkersData()
			
			; Save the track and convert it to a 3DS mesh.
			If (trackMesh = 0)
				
				CreateAndSave3DS()
				
			Else 
				
				Save3DS()
				DestroyTrack()
				
			EndIf 
			
			; Reset markers.
			ResetMarkersData()
			ResetMarkersPosition()
			
			ResetPoints()
			
			; Reset values.
			ResetValues()
			
			; Load 3DS track's mesh.
			Load3DSTrack()
			
			; Set editor's state.
			editorState = 1
			
		Else 
			
			; Switch to track editor.
			
			; Reset object (put them away).
			ResetObjects()
			
			; Destroy track's mesh.
			DestroyTrack()
			
			; Load markers data.
			LoadMarkersData()
			
			; Set editor's state.
			editorState = 0
			
		EndIf
		
		; Enable/disable GUI elements.
		SetGuiState(editorState)
		
	EndIf 
	
	; Exit.
	If (GUI_AppEvent() = btnExit)
		
		running = False
		
	EndIf
	
	; TRACK
	
	If (editorState = 0)
	
		GUI_Message(lblCurrentMarkerIndex, "settext", "Current marker index: " + currentMarkerIndex)
		GUI_Message(lblNumMarkersPlaced, "settext", "Marker placed: " + numMarkersPlaced)
		
		; TERRAIN 
		
		; Get selected heightmap.
		If (selectedHeightmap <> GUI_Message(lstHeightmaps, "getselected"))
			
			selectedHeightmap = GUI_Message(lstHeightmaps, "getselected")
			GUI_Message(imgHeightmap, "setimage", heightmapsPaths$(selectedHeightmap))
			
		EndIf 
		
		; Update scale values reading them from the spinners.
		terrainHScale = Int(GUI_Message(spnHorizontalScaleFactor, "getvalue"))
		terrainVScale = Int(GUI_Message(spnVerticalScaleFactor, "getvalue"))
		
		; Get selected tile.
		If (selectedTile <> GUI_Message(lstTiles, "getselected"))
			
			selectedTile = GUI_Message(lstTiles, "getselected")
			GUI_Message(imgTile, "setimage", tilesPaths$(selectedTile))
			
		EndIf
		
		; Load the terrain.
		If (GUI_AppEvent() = btnLoadTerrain)
			
			; Load terrain.
			LoadNewTerrain()
			
		EndIf 
		
	Else 
	
		; SCENE
		
		; Get selected object.
		If (selectedType <> GUI_Message(lst3DObjects, "getselected"))
			
			DebugLog("Object selected: Type " + selectedType + ", No. " + objectsPlaced(selectedType) + ".")
			
			If( objectIsPlaced = False )
				
				;object follow the arrow so we can change the object at fly
				
				If objectsPlaced(selectedType) <> MaxObjectPerType - 1
					ResetObject(selectedType, objectsPlaced(selectedType))
				EndIf
				
			Else
				;object is placed but we want to change object mesh 
				newSelectedType = GUI_Message(lst3DObjects, "getselected")
				
				obj = objects(selectedType, objectsPlaced(selectedType))
				
				PositionEntity objects(newSelectedType,objectsPlaced(newSelectedType)),EntityX(obj),EntityY(obj),EntityZ(obj)
				
				ResetObject(selectedType, objectsPlaced(selectedType))
				
			EndIf
			
			ResetSliders()
			
			selectedType = GUI_Message(lst3DObjects, "getselected")
			
		EndIf 
		
		; Add a new object.
		If (GUI_AppEvent() = btnAddObject)
			
			If ( objectsPlaced(selectedType) + 1 ) < MaxObjectPerType
				
				addObject = 1
				
				
				GUI_Message(btnAddObject, "setenabled",False)
				
				ResetSliders()
				
			EndIf
			
		EndIf
		
		; 3D OBJECTS
		
		If (GUI_Message(chkObjectScale, "getchecked"))
				
			scale# = (GUI_Message(sldObjectScale, "getvalue"))
			
			GUI_Message(sldObjectXScale, "setvalue", scale#)
			GUI_Message(sldObjectYScale, "setvalue", scale#)
			GUI_Message(sldObjectZScale, "setvalue", scale#)
				
			GUI_Message(sldObjectScale, "setenabled", True)
			GUI_Message(sldObjectXScale, "setenabled", False)
			GUI_Message(sldObjectYScale, "setenabled", False)
			GUI_Message(sldObjectZScale, "setenabled", False)
				
			xScale# = scale# 
			yScale# = scale# 
			zScale# = scale# 
				
			scaleIsChecked = 0
				
		Else
				
			If (GUI_Message(chkObjectScale, "getchecked") = 0 And (scaleIsChecked = 0))
					
				GUI_Message(sldObjectScale, "setenabled", False)
				GUI_Message(sldObjectXScale, "setenabled", True)
				GUI_Message(sldObjectYScale, "setenabled", True)
				GUI_Message(sldObjectZScale, "setenabled", True)
					
				GUI_Message(sldObjectXScale, "setvalue", scale#)
				GUI_Message(sldObjectYScale, "setvalue", scale#)
				GUI_Message(sldObjectZScale, "setvalue", scale#)
					
				scaleIsChecked = 1
					
			EndIf
				
			xScale# = (GUI_Message(sldObjectXScale, "getvalue"))
			yScale# = (GUI_Message(sldObjectYScale, "getvalue"))
			zScale# = (GUI_Message(sldObjectZScale, "getvalue"))
				
		EndIf
			
		rotation# = Int(GUI_Message(sldObjectRotation, "getvalue"))
			
		GUI_Message(lblObjectScaleValue, "settext",  Left("" + scale#, 4) + "x")
		GUI_Message(lblObjectXScaleValue, "settext", Left("" + xScale#, 4) + "x")
		GUI_Message(lblObjectYScaleValue, "settext", Left("" + yScale#, 4) + "x")
		GUI_Message(lblObjectZScaleValue, "settext", Left("" + zScale#, 4) + "x")
		GUI_Message(lblObjectRotationValue, "settext", "" + rotation# + "�")
			
		objectsScaleX#(selectedType, objectsPlaced(selectedType)) = xScale#
		objectsScaleY#(selectedType, objectsPlaced(selectedType)) = yScale# 
		objectsScaleZ#(selectedType, objectsPlaced(selectedType)) = zScale#
			
		objectsRotationY(selectedType, objectsPlaced(selectedType)) = rotation#
		
	EndIf 
	
End Function

; -----------------------------------------------------------------------------------
;~IDEal Editor Parameters:
;~F#19D#1BF#205#23E#250#26A#281#28E#29F#2A6#2AE#2BB#2C8#309#319#32E#354#364#37B#3A5
;~F#3E4#40C#421#434#44A#453#481#492#4A1#56A#581#5AE#5B9#5C4#5CD#5F7#62A#640#65A#664
;~F#66B#676#68C#6A1#6CE#6E8#701#708#754#763#78E#79B#7C0#82B#85B#88D#893#8BF#8C9#925
;~F#92F
;~C#Blitz3D