Get-ChildItem -File -Recurse | % { Rename-Item -Path $_.PSPath -NewName $_.Name.replace(".txt",".glsl")}
