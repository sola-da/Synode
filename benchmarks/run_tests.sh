rm resources/sink-values.txt
rm resources/out.txt
rm resources/out-without-synode.txt

sudo ./node_modules/n/bin/n 5.7.1
node ./TestsNode5.7.1.js
sudo ./node_modules/n/bin/n 0.10.47
node ./TestsNode0.10.47.js
mv resources/out.txt resources/out-without-synode.txt

synode ./node_modules/gm
synode ./node_modules/libnotify
synode ./node_modules/codem-transcode
synode ./node_modules/printer
synode ./node_modules/mixin-pro
synode ./node_modules/modulify
synode ./node_modules/mol-proto
synode ./node_modules/mongoosify
synode ./node_modules/mobile-icon-resizer
synode ./node_modules/m-log
synode ./node_modules/mongo-parse
synode ./node_modules/mongoosemask
synode ./node_modules/mongui
synode ./node_modules/mongo-edit
synode ./node_modules/mock2easy
synode ./node_modules/growl
synode ./node_modules/autolint/
synode ./node_modules/autolint/node_modules/growl
synode ./node_modules/mqtt-growl/
synode ./node_modules/mqtt-growl/node_modules/growl
synode ./node_modules/chook-growl-reporter/
synode ./node_modules/chook-growl-reporter/node_modules/growl
synode ./node_modules/bungle/
synode ./node_modules/fish
synode ./node_modules/git2json
synode ./node_modules/kerb_request
synode ./node_modules/keepass-dmenu

sudo ./node_modules/n/bin/n 5.7.1
node ./TestsNode5.7.1.js
sudo ./node_modules/n/bin/n 0.10.47
node ./TestsNode0.10.47.js
echo "Tests finished: check out resources/out.txt, resources/out-without-synode.txt and resources/sink-values.txt"
