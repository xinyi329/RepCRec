#!/usr/bin/env bash
mkdir -p output
for i in `seq 21`
do
    echo "Running test${i}"
    java -jar RepCRec.jar input/test${i} > output/out${i}
done