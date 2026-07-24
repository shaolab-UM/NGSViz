#!/bin/bash
export PATH=/opt/homebrew/bin:$PATH
cd /Users/benche/Desktop/myProj/ngsPlot/NGSViz
mvn --version
mvn test -Dtest=BedDBProcessTest,QueryGenomeRangeTest --batch-mode -q
