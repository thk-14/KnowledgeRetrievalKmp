package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import knowledgeretrievalkmp.composeapp.generated.resources.Res
import knowledgeretrievalkmp.composeapp.generated.resources.delete
import knowledgeretrievalkmp.composeapp.generated.resources.edit
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ConversationBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss()
        },
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(0.5f)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .clickable {
                        onRename()
                    }
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.edit),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.top_bar_icon_size),
                )
                Text("Rename")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .clickable {
                        onDelete()
                    }
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.delete),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.top_bar_icon_size),
                )
                Text("Delete")
            }
        }
    }
}

@Composable
fun MessageBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    message: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(10.dp)
                .width(50.dp)
                .height(60.dp)
        ) {
            Text(
                text = message
            )
        }
    }
}