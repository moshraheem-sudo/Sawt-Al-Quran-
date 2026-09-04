with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'r') as f:
    lines = f.readlines()

# Find the end of SettingsDialog composable and functions after it
open_folder_idx = -1
for i, line in enumerate(lines):
    if "fun openFolder(" in line:
        open_folder_idx = i
        break

if open_folder_idx != -1:
    # Delete the extra "}" we added at the end of the file
    if lines[-1].strip() == "}":
        lines.pop()

    # We need to make sure the brackets before `fun openFolder(` close `SettingsDialog` properly.
    # The structure is:
    #                     Spacer(modifier = Modifier.height(48.dp))
    #                     Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    #                 }
    #             }
    #         }
    #     }
    # }
    
    # Let's find "Spacer(modifier = Modifier.windowInsetsBottomHeight"
    spacer_idx = -1
    for i in range(open_folder_idx - 15, open_folder_idx):
        if "Spacer(modifier = Modifier.windowInsetsBottomHeight" in lines[i]:
            spacer_idx = i
            break
            
    if spacer_idx != -1:
        # replace everything between spacer_idx and open_folder_idx with exactly 5 closing braces
        new_lines = lines[:spacer_idx + 1]
        new_lines.extend([
            "                }\n",
            "            }\n",
            "        }\n",
            "    }\n",
            "}\n"
        ])
        new_lines.extend(lines[open_folder_idx:])
        
        with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'w') as f:
            f.writelines(new_lines)
        print("Fixed brackets!")
