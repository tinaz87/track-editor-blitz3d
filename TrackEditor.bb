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
;HidePointer()

; -----------------------------------------------------------------------------------


; --- CONST VALUES ------------------------------------------------------------------

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

; Max loadable scene objects.
Const MaxObjectTypes		= 50
Const MaxObjectPerType 		= 20 

; -----------------------------------------------------------------------------------


; -----------------------------------------------------------------------------------


; --- PATHS -------------------------------------------------------------------------

Const MediaPath$ 		= ".\Media\"
Const HeightMapsPath$ 	= ".\Media\HeightMaps\"
Const TilesPath$ 		= ".\Media\Tiles\"
Const ObjectsPath$		= ".\Media\3DObjects\"
Const TexturesPath$		= ".\Media\Textures\"
Const IconsPath$		= ".\Media\Icons\"

; --- VARIABLES ---------------------------------------------------------------------

; Editor's state.
; 
; Legend:
;			0 = Track's editor
;			1 = Edit the scene
Global editorState			= 0

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

; Track's mesh.
Global track				= 0

; -----------------------------------------------------------------------------------


; --- DEVIL GUI'S VARIABLES ---------------------------------------------------------

; object rotation
Global sldRotation
Global lblRotation


; heigthmap
Global lblScaleHmX
Global lblScaleHmY
Global hmScaleWidth = 0
Global hmScaleHeight = 0
Global sldWidthHm
Global sldHeightHm
Global loadHeightmap = False

;button load heightmap with texture in scene 
Global btnLH

;button add object in scene
Global btnObjSelected

; Texture part
Dim textureArray(20)
;Dim textureStringArray$(20)
Dim texturePath$(20)

;Heightmap part
Dim hmArray(20)
;Dim hmStringArray$(20)
Dim hmPath$(20)

;Object part

Dim objects(MaxObjectTypes, MaxObjectPerType)

Dim objStringArray$(20)

Dim typeObject#(300)
Dim aum_object(50)

;;Property Object
Dim xObject#(300)
Dim yObject#(300)
Dim zObject#(300)

Dim rotObject#(300)
Dim dxObject#(300)
Dim dyObject#(300)
Dim dzObject#(300)

;Window Object

; listBox
Global lstboxHM
Global lstboxTexture

Global terrainGroup

Global imgHM
Global imgTx

Global itemSelected
Global itemSelectedTx

Global lstboxObjects
Global propertyObjectGroup

;slider part
Global sldG
Global sldZ
Global sldX
Global sldY
Global sldGvalue
Global sldXvalue
Global sldYvalue
Global sldZvalue

;label part
Global lblScaleXText
Global lblScaleYText
Global lblScaleZText


Global lblCurrentMarkerIndex
Global lblNumMarkersPlaced

Global icoBtnLoad
Global icoBtnSave
Global icoBtnExit

Global addObject = 0



Global lblScaleText
Const firstSkin$ = "MacOS"
GUI_InitGUI("dgui_1.6\Skins\" + firstSkin$ + ".skin")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

Global numero_oggetto = 0
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


; -----------------------------------------------------------------------------------


; --- INIT --------------------------------------------------------------------------; 

; Init the GUI.
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
InitPlane()

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

	While Not KeyDown(1)
		
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
	
	posObjects()
	
	UpdateObjects()
	
End Function

; Load resources.
Function LoadResources()
	
	; Textures set.
	tex = LoadTexture(TexturesPath$ + "base_0.png")
	
	
	LoadObjects()
	
End Function 

; Create the plane.
Function InitPlane()
	
	;plane = CreateCube()
	;ScaleEntity plane, 1000, 1, 1000
	;EntityPickMode plane, 2  
	
	plane = CreatePlane()
	EntityPickMode plane, 2
	
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
		
	Next
	
End Function

; Destroy auto-gen. points.
Function DestroyAutoGenPoints()
	
	For n = 0 To AutoGenPointsNumber - 1
		
		FreeEntity autoGenPoints(n)
		
		FreeEntity autoGenPointsSX(n)
		FreeEntity autoGenPointsDX(n)
		
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
	
	outFile = WriteFile("Track.txt")
	
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
	
	If FileType("Track.txt")
		
		inFile = ReadFile("Track.txt")
		
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
	
	SaveMesh3DS(trackMesh,"Track.3ds")
	
	DestroyTrack()
	
End Function

; Save the track (markers and 3DS).
Function SaveTrack()
	
	SaveTrack()
	
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
			GUI_Message(lstboxObjects, "AddItem", -1, filename$)
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
Function LoadHeightmapTextures()
	
	index = 0
	dir = ReadDir(HeightMapsPath$)
	
	Repeat
		
		f$ = NextFile(dir)
		If f$ = "" Then Exit
		If ((FileType(HeightMapsPath$ + f$) = 1) And (f$ <> ".") And (f$ <> "..")) Then
			filename$ = Left(f$, Len(f$) - 4)
			GUI_Message(lstboxHM, "AddItem", -1, filename$)
			file$ = HeightMapsPath$ + f$
			hmArray(index) = LoadTexture(file$)
			hmPath$(index) = file$
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
			GUI_Message(lstboxTexture, "AddItem", -1, filename$)
			file$ = TilesPath$ + f$
			textureArray(index) = LoadTexture(file$)
			texturePath$(index) = file$
			index = index + 1
		EndIf
		
	Forever
	
	CloseDir dir
	
End Function

; Update objects.
Function UpdateObjects()
	
	For n = 0 To maxObjecttype - 1
		
		aum_object(n) = -1 ; contatore dell'oggetto di quel tipo = 1
		
		If (objects(n,0) > 0) ; se l'oggetto esiste
			For k=0 To maxObjectXtype
				PositionEntity objects(n,k),10000,0,0 ; posiziono inizialmente gli oggetti molto lontani (virtualmente all'infinito)
				EntityColor objects(n,k),255,255,255
			Next
		EndIf
	Next
	
	For n = 0 To 299		
		If xObject#(n) <>0
			
			tipo=typeObject#(n)
			
			If objects(tipo,0)>0
				aum_object(tipo)=aum_object(tipo)+1
				
				If aum_object(tipo) > maxObjectXtype Then aum_object(tipo) = maxObjectXtype
				
				PositionEntity objects(tipo,aum_object(tipo)),xObject#(n),yObject#(n),zObject#(n)
				
				RotateEntity objects(tipo,aum_object(tipo)),0,rotObject#(n),0
				
				ScaleEntity objects(tipo,aum_object(tipo)),1+dxObject#(n),1+dyObject#(n),1+dzObject#(n)
				
				If n=numero_oggetto
					EntityColor objects(tipo,aum_object(tipo)),155+Rnd(100),155,155
				EndIf
			EndIf
		EndIf
	Next
	
End Function

; Position objects.
Function posObjects()
	
	If KeyDown(211) Or KeyDown(14) ; se viene premuto CANC o BACK vengono azzerati gli oggetti
		xObject#(numero_oggetto) = 0
	EndIf
	
	If addObject = 1 
		
		CameraPick(camera,MouseX(),MouseY())
		;TerrainHeight(plane,PickedX(),PickedZ())
		xObject#(numero_oggetto)=PickedX#()
		yObject#(numero_oggetto)=PickedY#()
		zObject#(numero_oggetto)=PickedZ#()
		DebugLog "X: "+PickedX#()
		DebugLog "Y: "+PickedY#()
		DebugLog "Z: "+PickedZ#()
		
		If MouseDown(2)
			
			For n=0 To 1000
				If xObject#(n)=0
					numero_oggetto = n
					
				;DebugLog numero_oggetto
					n = 100000
				EndIf
			Next
			addObject = 0
			
		EndIf
		
	EndIf
	
;	;If KeyDown(59) ; tasto F1 ; cerca il primo oggetto disponibile
;	mouse_wheel = MouseZSpeed()
;	
;	If mouse_wheel = 1
;		
;		For n=0 To 1000
;			If xObject#(n)=0
;				numero_oggetto = n
;				
;				;DebugLog numero_oggetto
;				n = 100000
;			EndIf
;		Next
;	EndIf
;	
;	;EndIf
;	
;	If KeyDown(211) Or KeyDown(14) ; se viene premuto CANC o BACK vengono azzerati gli oggetti
;		xObject#(numero_oggetto) = 0
;	EndIf
;	
;	If KeyDown(157) And MouseDown(1) ; se premo CTRLDX e MOUSE SX
;		
;		valorecampick = CameraPick(camera,MouseX(),MouseY()) ; prendo il valore di dove clicco col mouse
;		
;		If valorecampick > 0 ; se è un valore valido, creo l'oggetto in quel punto
;			xObject#(numero_oggetto)=PickedX#()
;			yObject#(numero_oggetto)=PickedY#()
;			zObject#(numero_oggetto)=PickedZ#()
;			
;			If yObject#(numero_oggetto) > 2 ; se l'oggetto è da attaccare in verticale (non è attaccato al terreno)
;				dnx#=PickedNX#() ; prende la normale nell'asse X
;				dny#=PickedNY#() ; prende la normale nell'asse Y
;				dnz#=PickedNZ#() ; prende la normale nell'asse Z
;				xObject#(numero_oggetto)=xObject#(numero_oggetto) + dnx# ; mi sposto dalla normale in modo da evitare z-fighting
;				yObject#(numero_oggetto)=yObject#(numero_oggetto) + dny#
;				zObject#(numero_oggetto)=zObject#(numero_oggetto) + dnz#
;			EndIf
;		EndIf
;	EndIf
	
	
End Function

; -----------------------------------------------------------------------------------


; --- DEVIL GUI ---------------------------------------------------------------------



Function CreateWindow()
	
	comWin = GUI_CreateWindow(ScreenWidth - 280, 0, 280, ScreenHeight, "Commands", "", False, False, True, False)
	;GUI_Message(comWin, "setLocked", True)
	
;	mnuFile = GUI_CreateMenu(comWin, "File")
;	GUI_CreateMenu(mnuFile, "New")
;	GUI_CreateMenu(mnuFile, "Open")
;	GUI_CreateMenu(mnuFile, "Save")
;	GUI_CreateMenu(mnuFile, "-")
;	GUI_CreateMenu(mnuFile, "Exit")
;	mnuEdit = GUI_CreateMenu(comWin, "Edit")
;	GUI_CreateMenu(mnuEdit, "Cut")
;	GUI_CreateMenu(mnuEdit, "Copy")
;	GUI_CreateMenu(mnuEdit, "Paste")
;	mnuHelp = GUI_CreateMenu(comWin, "?")
;	GUI_CreateMenu(mnuHelp, "Help")
;;	GUI_CreateMenu(mnuHelp, "About")
;	
;	btnLoad = GUI_CreateButton(comWin, 180, 10,90, 25, "Load Track")
;	btnSave = GUI_CreateButton(comWin, 180, 40,90, 25, "Save Track")
;	
	; Track
	
	trackGroup = GUI_CreateGroupBox(comWin, 10, 5, 270, 50, "Menu")
	
	icoBtnSave = GUI_CreateButton(trackGroup, 10, 20, 40, 20,"", IconsPath$ + "filenew.png")
	icoBtnSave = GUI_CreateButton(trackGroup, 55, 20, 40, 20,"", IconsPath$ + "save.png")
	icoBtnSave = GUI_CreateButton(trackGroup, 100, 20, 40, 20,"", IconsPath$ + "folder.png")
	
	
	;GROUP TERRAIN
	down = 40
	
	;HEIGHTMAP
	terrainGroup = GUI_CreateGroupBox(comWin, 10, 50, 270, 390, "Terrain")
	lstboxHM = GUI_CreateListBox(terrainGroup,25,25 + down,80,100)
	
	lblCurrentMarkerIndex = GUI_CreateLabel(terrainGroup, 25 , 20, "Current Marker Index	: " + currentMarkerIndex)
	lblNumMarkersPlaced = GUI_CreateLabel(terrainGroup, 25 , 37, "Nr Marker Placed	: " + numMarkersPlaced )
	
	
	LoadHeightmapTextures()
	
	imgHM = GUI_CreateImage(terrainGroup, 125, 25 + down, 100, 100, hmPath(itemSelected))
	
	;Scale Heightmap
	
	lblScale_X = GUI_CreateLabel(terrainGroup, 25 , 140 + down, "Scale Width")
	sldWidthHm = GUI_CreateSlider(terrainGroup, 95 , 143 + down,100)
	lblScaleHmX = GUI_CreateLabel(terrainGroup, 215 , 136 + down, ""+hmScaleWidth + "%")
	
	
	lblScale_Y = GUI_CreateLabel(terrainGroup, 25 , 160 + down, "Scale Height")
	sldHeightHm = GUI_CreateSlider(terrainGroup, 95 , 163 + down,100)
	lblScaleHmY = GUI_CreateLabel(terrainGroup, 215 , 156 + down, ""+hmScaleHeight + "%")
	
	
	;TEXTURE
	
	lstboxTexture = GUI_CreateListBox(terrainGroup,25,190 + down ,80,100)
	
	LoadTiles()
	
	imgTx = GUI_CreateImage(terrainGroup, 125, 190 + down , 100, 100, texturePath(itemSelectedTx))
	
	btnLH = GUI_CreateButton(terrainGroup, 30, 310 + down ,190, 25, "Load HeightMap")
	
	;GROUP OBJECT
	
	propertyObjectGroup = GUI_CreateGroupBox(comWin, 10, 445, 270, 250, "Property Object")
	
	lstboxObjects = GUI_CreateListBox(propertyObjectGroup,30,20,220,60)
	
	btnObjSelected = GUI_CreateButton(propertyObjectGroup, 30, 90,60, 25, "Add Object")
	
	lblScaleX = GUI_CreateLabel(propertyObjectGroup, 33, 120, "Scale_X")
	sldX = GUI_CreateSlider(propertyObjectGroup, 35 , 140 ,150)
	lblScaleXText = GUI_CreateLabel(propertyObjectGroup, 188 , 133, "0%")
	
	lblScaleY = GUI_CreateLabel(propertyObjectGroup, 33, 150, "Scale_Y")
	sldY = GUI_CreateSlider(propertyObjectGroup, 35 , 170 ,150)
	lblScaleYText = GUI_CreateLabel(propertyObjectGroup, 188 , 163, "0%")
	
	lblScaleZ = GUI_CreateLabel(propertyObjectGroup, 33, 180, "Scale_Z")
	sldZ = GUI_CreateSlider(propertyObjectGroup, 35 , 200 ,150)
	lblScaleZText = GUI_CreateLabel(propertyObjectGroup, 188 , 193, "0%")
	
	lblRot = GUI_CreateLabel(propertyObjectGroup, 33, 210, "Rotation")
	sldRotation = GUI_CreateSlider(propertyObjectGroup, 35 , 230 ,150,0,0,359)
	lblRotation = GUI_CreateLabel(propertyObjectGroup, 188 , 223, "0°")
	
	
	GUI_Message(btnObjSelected, "setenabled", False)
	
	GUI_Message(sldX, "setenabled", False)
	GUI_Message(sldY, "setenabled", False)
	GUI_Message(sldZ, "setenabled", False)
	
	GUI_Message(sldRotation, "setenabled", False)
	
End Function

Function UpdateWindow()
	
	If btnLoad = GUI_AppEvent()
		;LOAD TRACK
	EndIf
	
	If btnSave = GUI_AppEvent()
		;SAVE TRACK
	EndIf
	
	; TERRAIN GROUP
	
	;Heightmap section
	hmScaleWidth = Int(GUI_Message(sldWidthHm, "GetValue"))
	hmScaleHeight = Int(GUI_Message(sldHeightHm, "GetValue"))
	
	If btnLH = GUI_AppEvent()
		loadHeightmap = True
	EndIf
	
	
	GUI_Message(lblScaleHmX, "SetText", "" + hmScaleWidth + "%")
	GUI_Message(lblScaleHmY, "SetText", "" + hmScaleHeight + "%")
	
	
	
	;insert string in listbox
	If itemSelected<>GUI_Message(lstboxHM, "getselected")
		
		itemSelected = GUI_Message(lstboxHM, "getselected")
		GUI_Message(imgHM,"setimage",hmPath(itemSelected))
		
	EndIf
	
	If itemSelectedTx<>GUI_Message(lstboxTexture, "getselected")
		
		
		itemSelectedTx = GUI_Message(lstboxTexture, "getselected")
		DebugLog itemSelectedTx
		var$ = texturePath(itemSelectedTx)
		If itemSelectedTx = 15
			Stop
		EndIf
		
		GUI_Message(imgTx,"setimage",texturePath(itemSelectedTx))
		
		
	EndIf
	
	
	; OBJECT GROUP
	
	gscale = 0
	
	;gscale = Int(GUI_Message(sldG, "GetValue"))
	xscale = Int(GUI_Message(sldX, "GetValue"))
	yscale = Int(GUI_Message(sldY, "GetValue"))
	zscale = Int(GUI_Message(sldZ, "GetValue"))
	rot = Int(GUI_Message(sldRotation, "GetValue"))
	
	
	;GUI_Message(lblScaleText, "SetText", "" + gscale + "%")
	GUI_Message(lblScaleZText, "SetText", "" + zscale + "%")
	GUI_Message(lblScaleXText, "SetText", "" + xscale + "%")
	GUI_Message(lblScaleYText, "SetText", "" + yscale + "%")
	GUI_Message(lblRotation, "SetText", "" + rot + "°")
	
	
	dxObject#(numero_oggetto) = Clamp(xscale + gscale,0,100)
	dyObject#(numero_oggetto) = Clamp(yscale + gscale,0,100)
	dzObject#(numero_oggetto) = Clamp(zscale + gscale,0,100)
	
	
	GUI_Message(sldX, "setvalue", Clamp(xscale + gscale - sldGvalue,0,100) )
	GUI_Message(sldY, "setvalue", Clamp(yscale + gscale - sldGvalue,0,100) )
	GUI_Message(sldZ, "setvalue", Clamp(zscale + gscale - sldGvalue,0,100) )
	
	;sldGvalue = gscale
	
	sldXvalue = xscale + gscale
	sldYvalue = yscale + gscale
	sldZvalue = yscale + gscale
	;------------
	; Devo modificare la posizione del selettore dello slider
	;-------------
	
	
	GUI_Message(lblCurrentMarkerIndex, "SetText", "Current Marker Index	: " + currentMarkerIndex )
	GUI_Message(lblNumMarkersPlaced, "SetText", "Nr Marker Placed			: " + numMarkersPlaced )
	
	;rotObject#(numero_oggetto) = barra#(rotObject#(numero_oggetto),800,220,200,30,"Angle",360)
	
	If btnObjSelected = GUI_AppEvent()
		;do something
	EndIf
	
	typeObject#(numero_oggetto) = GUI_Message(lstboxObjects, "getselected")
	
	;If (GUI_AppEvent() <> -1) Then DebugLog GUI_AppEvent()
	
End Function

; -----------------------------------------------------------------------------------

;Global ground = LoadTerrain("heightmap.jpg")
;ScaleEntity ground,5,50,5
;PositionEntity ground,-0,-1,-0
;Global tex = LoadTexture("terr_dirt-grass.jpg")
;ScaleTexture tex,1,1
;EntityTexture ground,tex
;EntityPickMode ground,2
;PositionEntity camera,10,50,10
;PointEntity camera,ground
;~IDEal Editor Parameters:
;~F#133#155#1A4#1B8#1C4#1CD#1D6#1E4#1F7#219#224#232#24A#289#2AF#2C0#2D2#2FE#308#3A3
;~F#3B8#3E2#3ED#3F6#3FD#408#448#462#47B#4A3#55E
;~C#Blitz3D