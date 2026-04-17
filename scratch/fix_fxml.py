import os

path = 'src/main/resources/blog/BlogDetail.fxml'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '<Label fx:id="readTimeLabel"'
replacement = '<Label fx:id="viewsLabel" text="👁 0 views" />\n                                           <Label fx:id="readTimeLabel"'

if target in content:
    new_content = content.replace(target, replacement)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Replacement successful")
else:
    print("Target not found")
