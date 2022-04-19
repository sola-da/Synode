rm resources/sink-values.txt
rm resources/out.txt
rm resources/out-without-synode.txt

sudo ./node_modules/n/bin/n 5.7.1
node ./TestsNode5.7.1.js
sudo ./node_modules/n/bin/n 0.10.47
node ./TestsNode0.10.47.js
#这一步就有out.txt了，此处只涉及到benchmark中的代码，那么先把这部分讲清楚。
mv resources/out.txt resources/out-without-synode.txt

# 然后dynamic和静态的好像在前述的模型分析中已经讲得很透彻，
#这里只需把它的实现讲清楚，从benchmark到静态的输出是什么，
#这一步又是如何输入到dynamic中实现动态部分的检测和自动修复的，整体逻辑讲清楚就行。
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
