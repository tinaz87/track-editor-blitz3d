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
Const GUISkin$ = "MacOS"

; Max number of points allowed.
Const MarkersNumber			= 5

; Max number of auto-generated points.
Const AutoGenPointsNumber 	= 500

; Mouse turn speed.
Const TurnSpeed#			= 0.8

; Curvature.
Const Curvature#			= 2

; Spline's step.
Const InterpolationStep#	= 25.0

; Marker's selection ray.
Const MarkerSelectionRay#	= 10.0

; Heightmaps size.
Const HeightmapsSize		= 512

; Default terrain's horizontal scale.
Const DefaultTerrainHScale	= 1

; Default terrain's vertical scale.
Const DefaultTerrainVScale	= 50

; Default terrain's tile scale.
Const DefaultTileScale		= 10

; Max number of heightmaps available.
Const MaxHeightmapsNumber	= 20

; Max number of tiles available.
Const MaxTilesNumber 		= 20

; Max loadable scene objects.
Const MaxObjectTypes		= 50
Const MaxObjectPerType 		= 20 

; -----------------------------------------------------------------------------------


; --- PATHS -------------------------------------------------------------------------

Const SavedDataPath$	= ".\TrackData\"

Const MediaPath$ 		= ".\Media\"
Const HeightMapsPath$ 	= ".\Media\HeightMaps\"
Const TilesPath$ 		= ".\Media\Tiles\"
Const ObjectsPath$		= ".\Media\3DObjects\"
Const TexturesPath$		= ".\Media\Textures\"
Const IconsPath$		= ".\Media\Icons\"

; -----------------------------------------------------------------------------------


; --- FILENAMES ---------------------------------------------------------------------

Const TrackData$		= "Track.txt"
Const Track3DS$			= "Track.3ds"
Const SceneData$		= "Scene.txt"

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

Dim autoGenPointsSXCoord#(AutoGenPointsNumber, 2)
Dim autoGenPointsDXCoord#(AutoGenPointsNumber, 2)

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

Global iaPoint				= 0

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

; Skydome.
Global skydome				= 0

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
Global selectedHeightmap	= 3

; Tiles.
Dim tiles(MaxTilesNumber)
Dim tilesPaths$(MaxTilesNumber)

; Selected tile.
Global selectedTile			= 0

; Terrain scales.
Global terrainHScale#		= DefaultTerrainHScale
Global terrainVScale#		= DefaultTerrainVScale

; Scene objects.
Dim objects(MaxObjectTypes, MaxObjectPerType)

; Objects placed.
Dim objectsPlaced(MaxObjectTypes)

; Objects properties.
Dim objectsPositionsX#(MaxObjectTypes * MaxObjectPerType)
Dim objectsPositionsY#(MaxObjectTypes * MaxObjectPerType)
Dim objectsPositionsZ#(MaxObjectTypes * MaxObjectPerType)

Dim objectsScaleX#(MaxObjectTypes * MaxObjectPerType)
Dim objectsScaleY#(MaxObjectTypes * MaxObjectPerType)
Dim objectsScaleZ#(MaxObjectTypes * MaxObjectPerType)

Dim objectsRotationY#(MaxObjectTypes * MaxObjectPerType)

; Selected type.
Global selectedType			= 0

; Selected object.
Global selectedObject		= 0

; If you're adding a new object to the scene, say 'true'.
Global addObject 			= 0

; -----------------------------------------------------------------------------------


; --- DEVIL GUI'S VARIABLES ---------------------------------------------------------

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
Global lblObjectScale, sldObjectScale, lblObjectScaleValue
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
;InitSkydome()

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
	
	;DebugLog "MARKERS PLACED: " + numMarkersPlaced
	;DebugLog "CURRENT POINT INDEX: " + currentMarkerIndex
	
	; Save/Load by pressing 'S' or 'L'.
	If KeyDown(31) Then SaveMarkers()
	If KeyDown(38) Then LoadMarkers()
	
	If KeyDown(88)
		
		SaveTrack()
		
		editorState = 1
		
	EndIf 
	
	; Update.
	Update()
	
End Function

; ### EDIT THE SCENE ####################################################
Function SceneEditorMode()
	
	MoveCamera()
	
	posObjects()
	
	UpdateObjects()
	
End Function

; Load resources.
Function LoadResources()
	
	; Textures set.
	tex = LoadTexture(TexturesPath$ + "base_0.png")
	
	LoadObjects()
	
End Function 

; Create a terrain using a specified heightmap.
Function CreateTerrain()
	
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
	
End Function

; Destroy the terrain.
Function DestroyTerrain()
	
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
	
End Function

; Destroy the current terrain and create a new one.
Function LoadNewTerrain()
	
	DestroyTerrain()
	
	CreateTerrain()
	
End Function 
	
; Create the plane.
Function InitGround()
	
	;plane = CreateCube()
	;ScaleEntity plane, 1000, 1, 1000
	;EntityPickMode plane, 2  
	
	plane = CreatePlane()
	PositionEntity plane, 0, -1, 0
	EntityPickMode plane, 2
	
End Function 

; Init the terrain.
Function InitTerrain()
	
	CreateTerrain()
	
End Function

; Create a camera.
Function InitCamera()
	
	camera = CreateCamera()
	PositionEntity camera, 0, 500, 0
	PointEntity camera, plane

End Function 

; Create and init the light.
Function InitLight()
	
	light = CreateLight()
	LightColor light, 100, 100, 100
	TurnEntity light, 90, 0, 0
	
End Function

; Create a skydome.
Function InitSkydome()
	
;	skydome = CreateSphere(24) 
;	ScaleEntity skydome, 2000, 2000, 2000 
;	FlipMesh skydome 
;	EntityFX skydome, 1 
;	skydomeTexture = LoadTexture("sky.jpg") 
;	EntityTexture skydome, skydomeTexture 
	
;	CameraRange camera, 1, 3000
	
End Function

; Init markers' meshes.
Function InitMarkers()
	
	
	For n = 0 To MarkersNumber - 1
		
		markersPlaced(n) = False
		
		markers(n) = CreateSphere(2)
		EntityColor markers(n), 60, 60, 60
		ScaleEntity markers(n), 5, 5, 5
		
		; Put the marker away.
		PositionEntity markers(n), 0, -1000, 0
		
	Next
	
End Function

; Init auto-generated points' meshes.
Function InitAutoGenPoints()
	
	; Main points.
	For n = 0 To AutoGenPointsNumber - 1
		
		autoGenPoints(n) = CreateSphere(2)
		EntityColor autoGenPoints(n), 80, 80, 80
		ScaleEntity autoGenPoints(n), 2, 2, 2
		
		; Put the marker away.
		PositionEntity autoGenPoints(n), 0, -1000, 0
		
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
		PositionEntity autoGenPointsSX(n), 0, -1000, 0
		PositionEntity autoGenPointsDX(n), 0, -1000, 0
		
	Next
	
End Function 

; Destroy markers.
Function DestroyMarkers()
	
	For n = 0 To MarkersNumber - 1
		
		FreeEntity markers(n)
		markers(n) = 0
		
	Next
	
End Function

; Destroy auto-gen. points.
Function DestroyAutoGenPoints()
	
	For n = 0 To AutoGenPointsNumber - 1
		
		FreeEntity autoGenPoints(n)
		
		autoGenPoints(n) = 0
		
		FreeEntity autoGenPointsSX(n)
		FreeEntity autoGenPointsDX(n)
		
		autoGenPointsSX(n) = 0
		autoGenPointsDX(n) = 0
		
	Next
	
End Function 

; Camera management.
Function MoveCamera()
	
	xx = KeyDown(205) - KeyDown(203)
	zz = KeyDown(200) - KeyDown(208)
	
	MoveEntity(camera, xx, 0, zz)
	
	mouseDeltaX# = MouseX() - mouseOldX
	mouseDeltaY# = MouseY() - mouseOldY
	
	; Check if you've pressed mouse DX.
	If MouseDown(2)
		
		TurnEntity(camera, mouseDeltaY# * TurnSpeed#, 0, 0, False)
		TurnEntity(camera, 0, mouseDeltaX# * -TurnSpeed#, 0, True)
		
	EndIf
	
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
	
	If MouseHit(1)
		
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
			
			PositionEntity markers(currentMarkerIndex), markersCoordX#(currentMarkerIndex), 0, markersCoordZ#(currentMarkerIndex)
			
			markersPlaced(currentMarkerIndex) = True
			
			updateNeeded = True
			
		EndIf
		
	EndIf 
	
End Function

; Reset markers.
Function ResetMarkersData()
	
	numMarkersPlaced 	= 0
	
	currentMarkerIndex 	= -1
	
	For n = 0 To MarkersNumber - 1
		
		markersCoordX#(n) = 0
		markersCoordY#(n) = 0
		markersCoordZ#(n) = 0
		
	Next
	
End Function
	
; Reset values.
Function ResetValues()
	
	iaPoint 			= 0
	
	numAutoGenPoints 	= 0
	
	autoCoordX# = markersCoordX#(0)
	autoCoordZ# = markersCoordZ#(0)
	
	xx# = markersCoordX#(0) - markersCoordX#(1)
	zz# = markersCoordZ#(0) - markersCoordZ#(1)
	
	autoAngle# = 270 - ATan2#(xx#, zz#)
	
End Function 

; Reset the position of the points.
Function ResetPoints()
	
	; Main points.
	For n = 0 To AutoGenPointsNumber - 1
		
		
		; Put the marker away.
		PositionEntity autoGenPoints(n), 0, -1000, 0
		
		; Put the marker away.
		PositionEntity autoGenPointsSX(n), 0, -1000, 0
		PositionEntity autoGenPointsDX(n), 0, -1000, 0
		
	Next
	
End Function

; Create track's mesh.
Function CreateTrack()
	
	DestroyTrack()
	
	; Create a new mesh.
	trackMesh = CreateMesh()
	trackSurface = CreateSurface(trackMesh)
	
	EntityPickMode trackMesh, 2
	
	; Add vertexes and triangles to the mesh.
	For n = 0 To numAutoGenPoints - 1
		
		surface = trackSurface
		
		If (n = numAutoGenPoints - 1)
			
			v0 = AddVertex(surface, autoGenPointsSXCoord#(n, 0), 2, autoGenPointsSXCoord#(n, 1), 0, 0)
			v1 = AddVertex(surface, autoGenPointsSXCoord#(0, 0), 2, autoGenPointsSXCoord#(1, 1), .25, 0)
			
			v2 = AddVertex(surface, autoGenPointsDXCoord#(n, 0), 2, autoGenPointsDXCoord#(n, 1), .25, .25)
			v3 = AddVertex(surface, autoGenPointsDXCoord#(0, 0), 2, autoGenPointsDXCoord#(0, 1), 0, .25)
			
		Else 
			
			v0 = AddVertex(surface, autoGenPointsSXCoord#(n, 0), 2, autoGenPointsSXCoord#(n, 1), 0, 0)
			v1 = AddVertex(surface, autoGenPointsSXCoord#(n + 1, 0), 2, autoGenPointsSXCoord#(n + 1, 1), .25, 0)
			
			v2 = AddVertex(surface, autoGenPointsDXCoord#(n, 0), 2, autoGenPointsDXCoord#(n, 1), .25, .25)
			v3 = AddVertex(surface, autoGenPointsDXCoord#(n + 1, 0), 2, autoGenPointsDXCoord#(n + 1, 1), 0, .25)
			
		EndIf 
		
		AddTriangle(surface, v0, v1, v2)
		AddTriangle(surface, v1, v3, v2)
		
	Next
	
	EntityColor trackMesh, 80, 80, 80
	EntityTexture trackMesh, tex
	
End Function 

; Destroy track's mesh.
Function DestroyTrack()
	
	; Clean an existing mesh.
	If (trackMesh > 0)
		
		FreeEntity trackMesh
		trackMesh = 0
		
	End If 
	
End Function 
	
; Update function ('You don't say?!' XD).
Function Update()
	
	If (updateNeeded = True)
		
		;--------------------------------------------------------------------------------------------
		
		; Reset values.
		ResetValues()
		
		;--------------------------------------------------------------------------------------------
		
		For n = 0 To numMarkersPlaced - 1
			
			PositionEntity markers(n), markersCoordX#(n), 0, markersCoordZ#(n)
			
			If (n = currentMarkerIndex - 1)
				EntityColor markers(n), 0, 255, 0
			Else
				EntityColor markers(n), 255, 0, 0
			EndIf 
			
		Next
		
		;--------------------------------------------------------------------------------------------
		
		While (iaPoint <= numMarkersPlaced)
			
			If (iaPoint = numMarkersPlaced)
				
				xx# = autoCoordX# - markersCoordX#(0)
				zz# = autoCoordZ# - markersCoordZ#(0)
				
			Else
				
				xx# = autoCoordX# - markersCoordX#(iaPoint)
				zz# = autoCoordZ# - markersCoordZ#(iaPoint)
				
			EndIf 
				
			Distance# = Sqr#(xx# * xx# + zz# * zz#)
			angle# = 270 - ATan2#(xx#, zz#)
			difference# = AngleDifference#(autoAngle#, angle#)
			
			If (difference# < -5)
				autoAngle# = autoAngle# - Curvature#
			EndIf 
			
			If (difference# > 5)
				autoAngle# = autoAngle# + Curvature#
			EndIf 
			
			If (Abs(difference#) < 5)
				autoAngle# = angle#
			EndIf 
			
			If (Distance# > 10)
				autoCoordX# = autoCoordX# + Cos#(autoAngle#)
				autoCoordZ# = autoCoordZ# + Sin#(autoAngle#)
			Else
				iaPoint = iaPoint + 1
			End If 
			
			ciclesCounter = ciclesCounter + 1
			
			If (ciclesCounter > InterpolationStep#)
				
				ciclesCounter = 0
				
				autoGenPointsCoordX#(numAutoGenPoints) = autoCoordX#
				autoGenPointsCoordZ#(numAutoGenPoints) = autoCoordZ#
				
				; PositionEntity 	autoGenPoints(numAutoGenPoints), 
				;					autoGenPointsCoordX#(numAutoGenPoints), 
				;					0, 
				;					autoGenPointsCoordZ#(numAutoGenPoints)
				
				numAutoGenPoints = numAutoGenPoints + 1 
				
				If (numAutoGenPoints > AutoGenPointsNumber - 1)
					
					numAutoGenPoints = AutoGenPointsNumber - 1
					
				End If 
				
			EndIf 
			
		Wend
		
		;--------------------------------------------------------------------------------------------
		
		; Calc. sx and dx points.
		For n = 0 To numAutoGenPoints - 1
			
			xx# = autoGenPointsCoordX#(n) - autoGenPointsCoordX#(n + 1)
			zz# = autoGenPointsCoordZ#(n) - autoGenPointsCoordZ#(n + 1)
			
			angles#(n) = 270 - ATan2#(xx#, zz#)
			
			xx# = autoGenPointsCoordX#(n) + Cos#(angles#(n) + 90) * 10
			zz# = autoGenPointsCoordZ#(n) + Sin#(angles#(n) + 90) * 10
			
			; Set SX point.
			autoGenPointsSXCoord#(n, 0) = xx#
			autoGenPointsSXCoord#(n, 1) = zz#
			
			xx# = autoGenPointsCoordX#(n) + Cos#(angles#(n) - 90) * 10
			zz# = autoGenPointsCoordZ#(n) + Sin#(angles#(n) - 90) * 10		
			
			; Set DX point.
			autoGenPointsDXCoord#(n, 0) = xx#
			autoGenPointsDXCoord#(n, 1) = zz#
			
		Next
		
		;--------------------------------------------------------------------------------------------
		
		; Reset the position of the points.
		ResetPoints()
		
		For n = 0 To numAutoGenPoints - 1
			
			; Position main points.
			PositionEntity autoGenPoints(n), autoGenPointsCoordX(n), 0, autoGenPointsCoordZ(n)
			
		Next 
		
		;--------------------------------------------------------------------------------------------
		
		If (renderTriangles = 1)
			
			CreateTrack()
			
		Else 
			
			DestroyTrack()
			
			For n = 0 To numAutoGenPoints - 1
				
				; Position SX and DX points.
				PositionEntity autoGenPointsSX(n), autoGenPointsSXCoord#(n, 0), 0, autoGenPointsSXCoord#(n, 1)
				PositionEntity autoGenPointsDX(n), autoGenPointsDXCoord#(n, 0), 0, autoGenPointsDXCoord#(n, 1)
				
			Next	
			
		EndIf 
			
		;--------------------------------------------------------------------------------------------
		
		updateNeeded = False 
		
	EndIf 
	
End Function 

; Save the current track to file.
Function SaveMarkers()
	
	outFile = WriteFile(SavedDataPath$ + TrackData$)
	
	For n = 0 To MarkersNumber - 1
		
		WriteByte(outFile, markersPlaced(n))
		
		WriteFloat(outFile, markersCoordX#(n))
		WriteFloat(outFile, markersCoordY#(n))
		WriteFloat(outFile, markersCoordZ#(n))
		
	Next
	
	CloseFile outFile
	
	DebugLog("Track saved!")
	
End Function 

; Load a track from file.
Function LoadMarkers()
	
	If (FileType(SavedDataPath$ + TrackData$))
		
		inFile = ReadFile(SavedDataPath$ + TrackData$)
		
		loadedPoints = 0
		
		For n = 0 To MarkersNumber - 1
			
			markersPlaced(n) = ReadByte(inFile)
			
			markersCoordX#(n) = ReadFloat(inFile)
			markersCoordY#(n) = ReadFloat(inFile)
			markersCoordZ#(n) = ReadFloat(inFile)
			
			If (markersPlaced(n) = True)
				
				loadedPoints = loadedPoints + 1
				
			End If 
			
		Next
		
		; Write how many points you've loaded successfully.
		DebugLog("LOAD: " + loadedPoints + " points loaded.")
		
		numMarkersPlaced = loadedPoints
		
		currentMarkerIndex = loadedPoints - 1
		
		CloseFile inFile
		
		; Reset values.
		ResetValues()
		
		; Track needs to be updated.
		updateNeeded = True
		
	EndIf
	
End Function

; Save the track as a 3DS object.
Function Save3DS()
	
	CreateTrack()
	
	SaveMesh3DS(trackMesh, SavedDataPath$ + Track3DS$)
	
	DestroyTrack()
	
End Function

; Save the track (markers and 3DS).
Function SaveTrack()
	
	SaveMarkers()
	
	Save3DS()
	
End Function

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

; Load objects.
Function LoadObjects()
	
	index = 0
	dir = ReadDir(ObjectsPath$)
	
	Repeat
		
		f$ = NextFile(dir)
		If f$ = "" Then Exit
		ext$ = Right(f$, 3)
		If ((FileType(ObjectsPath$ + f$) = 1) And (f$ <> ".") And (f$ <> "..") And (ext$ = "3ds" Or ext$ = "b3d")) Then
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
			
			PositionEntity objects(n, 0), 0, -1000, 0
			
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
Function posObjects()
	
	If addObject = 1
		
		CameraPick(camera,MouseX(),MouseY())
		
		currentObject = objects(selectedType, objectsPlaced(selectedType))
		
		ScaleEntity currentObject, objectsScaleX#(selectedObject), objectsScaleY#(selectedObject), objectsScaleZ#(selectedObject)
		
		PositionEntity currentObject, PickedX#(), PickedY#(), PickedZ#()
		
		RotateEntity currentObject, 0, objectsRotationY#(selectedObject), 0
		
		If MouseDown(3) ; Mouse Wheel Button
			
			objectsPositionsX#(selectedObject) = PickedX#()
			objectsPositionsY#(selectedObject) = PickedY#()
			objectsPositionsZ#(selectedObject) = PickedZ#()
			objectsPlaced(selectedType) = objectsPlaced(selectedType) + 1 
			addObject = 0
			
		EndIf
		
	EndIf
	
End Function

; -----------------------------------------------------------------------------------


; --- DEVIL GUI ---------------------------------------------------------------------

Function InitGUI()
	
	GUI_InitGUI("dgui_1.6\Skins\" + GUISkin$ + ".skin")
	
End Function

Function CreateWindow()
	
	comWin = GUI_CreateWindow(ScreenWidth - 280, 0, 280, ScreenHeight, "Commands", "", False, False, True, False)
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
	sldObjectScale = GUI_CreateSlider(grpObjectProperties, 75, 25, 140, 1, 1, 100)
	lblObjectScaleValue = GUI_CreateLabel(grpObjectProperties, 220, 20, "1.0x")
	
	lblObjectXScale = GUI_CreateLabel(grpObjectProperties, 25, 45, "X Scale")
	sldObjectXScale = GUI_CreateSlider(grpObjectProperties, 75, 50, 140, 1, 1, 100)
	lblObjectXScaleValue = GUI_CreateLabel(grpObjectProperties, 220, 45, "1.0x")
	
	lblObjectYScale = GUI_CreateLabel(grpObjectProperties, 25, 70, "Y Scale")
	sldObjectYScale = GUI_CreateSlider(grpObjectProperties, 75, 75, 140, 1, 1, 100)
	lblObjectYScaleValue = GUI_CreateLabel(grpObjectProperties, 220, 70, "1.0x")
	
	lblObjectZScale = GUI_CreateLabel(grpObjectProperties, 25, 95, "Z Scale")
	sldObjectZScale = GUI_CreateSlider(grpObjectProperties, 75, 100, 140, 1, 1, 100)
	lblObjectZScaleValue = GUI_CreateLabel(grpObjectProperties, 220, 95, "1.0x")
	
	lblObjectRotation = GUI_CreateLabel(grpObjectProperties, 25, 120, "Rotation")
	sldObjectRotation = GUI_CreateSlider(grpObjectProperties, 75, 125, 140, 0, 0, 360)
	lblObjectRotationValue = GUI_CreateLabel(grpObjectProperties, 220, 120, "0�")
	
End Function

Function ResetSliders()
	
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
			
			; Reset data.
			ResetMarkersData()
			
			; Reset values.
			ResetValues()
			
			; Update the track.
			updateNeeded = True
			
		Else 
			
			; New scene.
			
		EndIf 
		
	EndIf
	
	; Load
	If (GUI_AppEvent() = btnLoad)
		
		If (editorState = 0)
			
			; Load track from file.
			LoadMarkers()
			
		Else 
			
			; Load scene from file.
			
		EndIf 
		
	EndIf
	
	; Save.
	If (GUI_AppEvent() = btnSave)
		
		If (editorState = 0)
			
			; Save track.
			SaveMarkers()
			
		Else 
			
			; Save scene.
			
		EndIf
		
	EndIf 
	
	; Switch editor mode.
	If (GUI_AppEvent() = btnSwitch)
		
		If (editorState = 0)
			
			; Switch to scene editor.
			
			; Save the track and convert it to a 3DS mesh.
			SaveTrack()
			
			; Destroy track and markers.
			DestroyTrack()
			ResetMarkersData()
			ResetPoints()
			
			; Load 3DS track's mesh.
			trackMesh = LoadMesh(SavedDataPath$ + Track3DS$)
			
			editorState = 1
			
		Else 
			
			; Switch to track editor.
			
			
		EndIf
		
	EndIf 
	
	; Exit.
	If (GUI_AppEvent() = btnExit)
		
		running = False
		
	EndIf
	
	; TRACK
	
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
	
	; SCENE
	
	; Get selected object.
	If (selectedType <> GUI_Message(lst3DObjects, "getselected"))
		
		
		ScaleEntity objects(selectedType,objectsPlaced(selectedType)),1,1,1
		
		PositionEntity objects(selectedType,objectsPlaced(selectedType)),0,-1000,-1000
		
		RotateEntity objects(selectedType,objectsPlaced(selectedType)),0,0,0
		
		selectedType = GUI_Message(lst3DObjects, "getselected")
		selectedObject = selectedType * MaxObjectPerType + objectsPlaced(selectedType)
		
		ResetSliders()
		
	EndIf 
	
	; Add a new object.
	If (GUI_AppEvent() = btnAddObject)
		
		addObject = 1
		
		;ResetSliders()
		
	EndIf
	
	; 3D OBJECTS
	
	xScale# = Int(GUI_Message(sldObjectXScale, "getvalue"))
	yScale# = Int(GUI_Message(sldObjectYScale, "getvalue"))
	zScale# = Int(GUI_Message(sldObjectZScale, "getvalue"))
	rotation# = Int(GUI_Message(sldObjectRotation, "getvalue"))
	
	GUI_Message(lblObjectXScaleValue, "settext", "" + xScale# + ".0x")
	GUI_Message(lblObjectYScaleValue, "settext", "" + yScale# + ".0x")
	GUI_Message(lblObjectZScaleValue, "settext", "" + zScale# + ".0x")
	GUI_Message(lblObjectRotationValue, "settext", "" + rotation# + "�")
	
	objectsScaleX#(selectedObject) = xScale#
	objectsScaleY#(selectedObject) = yScale# 
	objectsScaleZ#(selectedObject) = zScale#
	
	objectsRotationY#(selectedObject) = rotation#
	
End Function

; -----------------------------------------------------------------------------------
;~IDEal Editor Parameters:
;~F#147#169#1B8#1C3#1CD#1E3#1F6#1FF#20C#213#21C#225#233#246#268#274#287#29F#2DE#304
;~F#315#326#338#364#371#40C#421#44D#458#461#468#473#489#4B3#4CD#4E6#4ED#50E#514#568
;~C#Blitz3D