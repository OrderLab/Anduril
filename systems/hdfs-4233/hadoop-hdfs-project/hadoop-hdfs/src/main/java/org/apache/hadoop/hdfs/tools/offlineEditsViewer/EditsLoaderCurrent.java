/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.tools.offlineEditsViewer;

import java.io.EOFException;
import java.io.IOException;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

import org.apache.hadoop.hdfs.protocol.LayoutVersion;
import org.apache.hadoop.hdfs.protocol.LayoutVersion.Feature;
import org.apache.hadoop.hdfs.server.namenode.FSEditLogOpCodes;

import static org.apache.hadoop.hdfs.tools.offlineEditsViewer.Tokenizer.ByteToken;
import static org.apache.hadoop.hdfs.tools.offlineEditsViewer.Tokenizer.IntToken;
import static org.apache.hadoop.hdfs.tools.offlineEditsViewer.Tokenizer.VIntToken;

/**
 * EditsLoaderCurrent processes Hadoop EditLogs files and walks over
 * them using a provided EditsVisitor, calling the visitor at each element
 * enumerated below.
 *
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
class EditsLoaderCurrent implements EditsLoader {

  private static int[] supportedVersions = { -18, -19, -20, -21, -22, -23, -24,
      -25, -26, -27, -28, -30, -31, -32, -33, -34, -35, -36, -37, -38, -39};

  private EditsVisitor v;
  private int editsVersion = 0;

  /**
   * Constructor
   */
  public EditsLoaderCurrent(EditsVisitor visitor) {
    v = visitor;
  }

  /**
   * Checks if this EditsLoader can load given version of edits
   *
   * @param version version to load
   * @return true if this EditsLoader can load given version of edits
   */
  @Override
  public boolean canLoadVersion(int version) {
    for(int v : supportedVersions) { if(v == version) return true; }
    return false;
  }

  /**
   * Visit a transaction ID, if the log version supports it.
   */
  private void visitTxId() throws IOException {
    if (LayoutVersion.supports(Feature.STORED_TXIDS, editsVersion)) {
      v.visitLong(EditsElement.TRANSACTION_ID);
    }
  }
  
  /**
   * Visit OP_INVALID
   */
  private void visit_OP_INVALID() throws IOException {
  }

  /**
   * Visit OP_ADD
   */
  private void visit_OP_ADD() throws IOException {
    visit_OP_ADD_or_OP_CLOSE(FSEditLogOpCodes.OP_ADD);
  }

  /**
   * Visit OP_CLOSE
   */
  private void visit_OP_CLOSE() throws IOException {
    visit_OP_ADD_or_OP_CLOSE(FSEditLogOpCodes.OP_CLOSE);
  }

  /**
   * Visit OP_ADD and OP_CLOSE, they are almost the same
   *
   * @param editsOpCode op code to visit
   */
  private void visit_OP_ADD_or_OP_CLOSE(FSEditLogOpCodes editsOpCode)
    throws IOException {
    visitTxId();
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      IntToken opAddLength = v.visitInt(EditsElement.LENGTH);
      // this happens if the edits is not properly ended (-1 op code),
      // it is padded at the end with all zeros, OP_ADD is zero so
      // without this check we would treat all zeros as empty OP_ADD)
      if (opAddLength.value == 0) {
        throw new IOException("OpCode " + editsOpCode
            + " has zero length (corrupted edits)");
      }
    }
    
    v.visitStringUTF8(EditsElement.PATH);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitShort(EditsElement.REPLICATION);
      v.visitLong(EditsElement.MTIME);
      v.visitLong(EditsElement.ATIME);
      v.visitLong(EditsElement.BLOCKSIZE);
    } else {
      v.visitStringUTF8(EditsElement.REPLICATION);
      v.visitStringUTF8(EditsElement.MTIME);
      v.visitStringUTF8(EditsElement.ATIME);
      v.visitStringUTF8(EditsElement.BLOCKSIZE);
    }
    // now read blocks
    IntToken numBlocksToken = v.visitInt(EditsElement.NUMBLOCKS);
    for (int i = 0; i < numBlocksToken.value; i++) {
      v.visitEnclosingElement(EditsElement.BLOCK);

      v.visitLong(EditsElement.BLOCK_ID);
      v.visitLong(EditsElement.BLOCK_NUM_BYTES);
      v.visitLong(EditsElement.BLOCK_GENERATION_STAMP);

      v.leaveEnclosingElement();
    }
    // PERMISSION_STATUS
    v.visitEnclosingElement(EditsElement.PERMISSION_STATUS);

    v.visitStringText( EditsElement.USERNAME);
    v.visitStringText( EditsElement.GROUPNAME);
    v.visitShort(      EditsElement.FS_PERMISSIONS);

    v.leaveEnclosingElement();
    if(editsOpCode == FSEditLogOpCodes.OP_ADD) {
      v.visitStringUTF8(EditsElement.CLIENT_NAME);
      v.visitStringUTF8(EditsElement.CLIENT_MACHINE);
    }
  }

  /**
   * Visit OP_RENAME_OLD
   */
  private void visit_OP_RENAME_OLD() throws IOException {
    visitTxId();
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitInt(EditsElement.LENGTH);
    }
    v.visitStringUTF8( EditsElement.SOURCE);
    v.visitStringUTF8( EditsElement.DESTINATION);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.TIMESTAMP);
    } else {
      v.visitStringUTF8(EditsElement.TIMESTAMP);
    }
  }

  /**
   * Visit OP_DELETE
   */
  private void visit_OP_DELETE() throws IOException {
    visitTxId();
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitInt(EditsElement.LENGTH);
    }
    v.visitStringUTF8( EditsElement.PATH);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.TIMESTAMP);
    } else {
      v.visitStringUTF8(EditsElement.TIMESTAMP);
    }
  }

  /**
   * Visit OP_MKDIR
   */
  private void visit_OP_MKDIR() throws IOException {
    visitTxId();
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitInt(EditsElement.LENGTH);
    }
    v.visitStringUTF8( EditsElement.PATH);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.TIMESTAMP);
      v.visitLong(EditsElement.ATIME);
    } else {
      v.visitStringUTF8(EditsElement.TIMESTAMP);
      v.visitStringUTF8(EditsElement.ATIME);
    }
    // PERMISSION_STATUS
    v.visitEnclosingElement( EditsElement.PERMISSION_STATUS);

    v.visitStringText( EditsElement.USERNAME);
    v.visitStringText( EditsElement.GROUPNAME);
    v.visitShort(      EditsElement.FS_PERMISSIONS);

    v.leaveEnclosingElement();
  }

  /**
   * Visit OP_SET_REPLICATION
   */
  private void visit_OP_SET_REPLICATION() throws IOException {
    visitTxId();

    v.visitStringUTF8(EditsElement.PATH);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitShort(EditsElement.REPLICATION);
    } else {
      v.visitStringUTF8(EditsElement.REPLICATION);
    }
  }

  /**
   * Visit OP_SET_PERMISSIONS
   */
  private void visit_OP_SET_PERMISSIONS() throws IOException {
    visitTxId();

    v.visitStringUTF8( EditsElement.PATH);
    v.visitShort(      EditsElement.FS_PERMISSIONS);
  }

  /**
   * Visit OP_SET_OWNER
   */
  private void visit_OP_SET_OWNER() throws IOException {
    visitTxId();

    v.visitStringUTF8(EditsElement.PATH);
    v.visitStringUTF8(EditsElement.USERNAME);
    v.visitStringUTF8(EditsElement.GROUPNAME);
  }

  /**
   * Visit OP_SET_GENSTAMP
   */
  private void visit_OP_SET_GENSTAMP() throws IOException {
    visitTxId();

    v.visitLong(EditsElement.GENERATION_STAMP);
  }

  /**
   * Visit OP_TIMES
   */
  private void visit_OP_TIMES() throws IOException {
    visitTxId();
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitInt(EditsElement.LENGTH);
    }
    v.visitStringUTF8( EditsElement.PATH);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.MTIME);
      v.visitLong(EditsElement.ATIME);
    } else {
      v.visitStringUTF8(EditsElement.MTIME);
      v.visitStringUTF8(EditsElement.ATIME);
    }
  }

  /**
   * Visit OP_SET_QUOTA
   */
  private void visit_OP_SET_QUOTA() throws IOException {
    visitTxId();

    v.visitStringUTF8( EditsElement.PATH);
    v.visitLong(       EditsElement.NS_QUOTA);
    v.visitLong(       EditsElement.DS_QUOTA);
  }

  /**
   * Visit OP_RENAME
   */
  private void visit_OP_RENAME() throws IOException {
    visitTxId();
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitInt(EditsElement.LENGTH);
    }
    v.visitStringUTF8(    EditsElement.SOURCE);
    v.visitStringUTF8(    EditsElement.DESTINATION);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.TIMESTAMP);
    } else {
      v.visitStringUTF8(EditsElement.TIMESTAMP);
    }
    v.visitBytesWritable( EditsElement.RENAME_OPTIONS);
  }

  /**
   * Visit OP_CONCAT_DELETE
   */
  private void visit_OP_CONCAT_DELETE() throws IOException {
    visitTxId();
    int sourceCount = 0;
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      IntToken lengthToken = v.visitInt(EditsElement.LENGTH);
      sourceCount = lengthToken.value - 2;
    }
    v.visitStringUTF8(EditsElement.CONCAT_TARGET);
    // all except of CONCAT_TARGET and TIMESTAMP
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      IntToken lengthToken = v.visitInt(EditsElement.LENGTH);
      sourceCount = lengthToken.value;
    }
    for(int i = 0; i < sourceCount; i++) {
      v.visitStringUTF8(EditsElement.CONCAT_SOURCE);
    }
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.TIMESTAMP);
    } else {
      v.visitStringUTF8(EditsElement.TIMESTAMP);
    }
  }

  /**
   * Visit OP_SYMLINK
   */
  private void visit_OP_SYMLINK() throws IOException {
    visitTxId();
    if (!LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitInt(EditsElement.LENGTH);
    }
    v.visitStringUTF8( EditsElement.SOURCE);
    v.visitStringUTF8( EditsElement.DESTINATION);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.MTIME);
      v.visitLong(EditsElement.ATIME);
    } else {
      v.visitStringUTF8(EditsElement.MTIME);
      v.visitStringUTF8(EditsElement.ATIME);
    }
    // PERMISSION_STATUS
    v.visitEnclosingElement(EditsElement.PERMISSION_STATUS);

    v.visitStringText( EditsElement.USERNAME);
    v.visitStringText( EditsElement.GROUPNAME);
    v.visitShort(      EditsElement.FS_PERMISSIONS);

    v.leaveEnclosingElement();
  }

  /**
   * Visit OP_GET_DELEGATION_TOKEN
   */
  private void visit_OP_GET_DELEGATION_TOKEN() throws IOException {
    visitTxId();
    
    v.visitByte(EditsElement.T_VERSION);
    v.visitStringText(EditsElement.T_OWNER);
    v.visitStringText(EditsElement.T_RENEWER);
    v.visitStringText(EditsElement.T_REAL_USER);
    v.visitVLong(EditsElement.T_ISSUE_DATE);
    v.visitVLong(EditsElement.T_MAX_DATE);
    v.visitVInt(EditsElement.T_SEQUENCE_NUMBER);
    v.visitVInt(EditsElement.T_MASTER_KEY_ID);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.T_EXPIRY_TIME);
    } else {
      v.visitStringUTF8(EditsElement.T_EXPIRY_TIME);
    }
  }

  /**
   * Visit OP_RENEW_DELEGATION_TOKEN
   */
  private void visit_OP_RENEW_DELEGATION_TOKEN()
    throws IOException {
    visitTxId();

    v.visitByte(EditsElement.T_VERSION);
    v.visitStringText(EditsElement.T_OWNER);
    v.visitStringText(EditsElement.T_RENEWER);
    v.visitStringText(EditsElement.T_REAL_USER);
    v.visitVLong(EditsElement.T_ISSUE_DATE);
    v.visitVLong(EditsElement.T_MAX_DATE);
    v.visitVInt(EditsElement.T_SEQUENCE_NUMBER);
    v.visitVInt(EditsElement.T_MASTER_KEY_ID);
    if (LayoutVersion.supports(Feature.EDITLOG_OP_OPTIMIZATION, editsVersion)) {
      v.visitLong(EditsElement.T_EXPIRY_TIME);
    } else {
      v.visitStringUTF8(EditsElement.T_EXPIRY_TIME);
    }
  }

  /**
   * Visit OP_CANCEL_DELEGATION_TOKEN
   */
  private void visit_OP_CANCEL_DELEGATION_TOKEN()
    throws IOException {
    visitTxId();

      v.visitByte(       EditsElement.T_VERSION);
      v.visitStringText( EditsElement.T_OWNER);
      v.visitStringText( EditsElement.T_RENEWER);
      v.visitStringText( EditsElement.T_REAL_USER);
      v.visitVLong(      EditsElement.T_ISSUE_DATE);
      v.visitVLong(      EditsElement.T_MAX_DATE);
      v.visitVInt(       EditsElement.T_SEQUENCE_NUMBER);
      v.visitVInt(       EditsElement.T_MASTER_KEY_ID);
  }

  /**
   * Visit OP_UPDATE_MASTER_KEY
   */
  private void visit_OP_UPDATE_MASTER_KEY()
    throws IOException {
    visitTxId();
    
      v.visitVInt(  EditsElement.KEY_ID);
      v.visitVLong( EditsElement.KEY_EXPIRY_DATE);
      VIntToken blobLengthToken = v.visitVInt(EditsElement.KEY_LENGTH);
      v.visitBlob(EditsElement.KEY_BLOB, blobLengthToken.value);
  }
  
  private void visit_OP_REASSIGN_LEASE()
    throws IOException {
    visitTxId();

      v.visitStringUTF8(EditsElement.CLIENT_NAME);
      v.visitStringUTF8(EditsElement.PATH);
      v.visitStringUTF8(EditsElement.CLIENT_NAME);
  }

  /**
   * Visit OP_BEGIN_LOG_SEGMENT
   */
  private void visit_OP_BEGIN_LOG_SEGMENT()
    throws IOException {
    visitTxId();
  }
  
  /**
   * Visit OP_END_LOG_SEGMENT
   */
  private void visit_OP_END_LOG_SEGMENT()
    throws IOException {
    visitTxId();
  }

  private void visitOpCode(FSEditLogOpCodes editsOpCode)
    throws IOException {

    switch(editsOpCode) {
      case OP_INVALID: // -1
        visit_OP_INVALID();
        break;
      case OP_ADD: // 0
        visit_OP_ADD();
        break;
      case OP_CLOSE: // 9
        visit_OP_CLOSE();
        break;
      case OP_RENAME_OLD: // 1
        visit_OP_RENAME_OLD();
        break;
      case OP_DELETE: // 2
        visit_OP_DELETE();
        break;
      case OP_MKDIR: // 3
        visit_OP_MKDIR();
        break;
      case OP_SET_REPLICATION: // 4
        visit_OP_SET_REPLICATION();
        break;
      case OP_SET_PERMISSIONS: // 7
        visit_OP_SET_PERMISSIONS();
        break;
      case OP_SET_OWNER: // 8
        visit_OP_SET_OWNER();
        break;
      case OP_SET_GENSTAMP: // 10
        visit_OP_SET_GENSTAMP();
        break;
      case OP_TIMES: // 13
        visit_OP_TIMES();
        break;
      case OP_SET_QUOTA: // 14
        visit_OP_SET_QUOTA();
        break;
      case OP_RENAME: // 15
        visit_OP_RENAME();
        break;
      case OP_CONCAT_DELETE: // 16
        visit_OP_CONCAT_DELETE();
        break;
      case OP_SYMLINK: // 17
        visit_OP_SYMLINK();
        break;
      case OP_GET_DELEGATION_TOKEN: // 18
        visit_OP_GET_DELEGATION_TOKEN();
        break;
      case OP_RENEW_DELEGATION_TOKEN: // 19
        visit_OP_RENEW_DELEGATION_TOKEN();
        break;
      case OP_CANCEL_DELEGATION_TOKEN: // 20
        visit_OP_CANCEL_DELEGATION_TOKEN();
        break;
      case OP_UPDATE_MASTER_KEY: // 21
        visit_OP_UPDATE_MASTER_KEY();
        break;
      case OP_REASSIGN_LEASE: // 22
        visit_OP_REASSIGN_LEASE();
        break;
      case OP_END_LOG_SEGMENT: // 23
        visit_OP_END_LOG_SEGMENT();
        break;        
      case OP_START_LOG_SEGMENT: // 24
        visit_OP_BEGIN_LOG_SEGMENT();
        break;
      default:
      {
        throw new IOException("Unknown op code " + editsOpCode);
      }
    }
  }

  /**
   * Loads edits file, uses visitor to process all elements
   */
  @Override
  public void loadEdits() throws IOException {

    try {
      v.start();
      v.visitEnclosingElement(EditsElement.EDITS);

      IntToken editsVersionToken = v.visitInt(EditsElement.EDITS_VERSION);
      editsVersion = editsVersionToken.value;
      if(!canLoadVersion(editsVersion)) {
        throw new IOException("Cannot process editLog version " +
          editsVersionToken.value);
      }

      FSEditLogOpCodes editsOpCode;
      do {
        v.visitEnclosingElement(EditsElement.RECORD);

        ByteToken opCodeToken;
        try {
          opCodeToken = v.visitByte(EditsElement.OPCODE);
        } catch (EOFException eof) {
          // Getting EOF when reading the opcode is fine --
          // it's just a finalized edits file
          // Just fake the OP_INVALID here.
          opCodeToken = new ByteToken(EditsElement.OPCODE);
          opCodeToken.fromByte(FSEditLogOpCodes.OP_INVALID.getOpCode());
          v.visit(opCodeToken);
        }
        editsOpCode = FSEditLogOpCodes.fromByte(opCodeToken.value);

        v.visitEnclosingElement(EditsElement.DATA);

        visitOpCode(editsOpCode);

        v.leaveEnclosingElement(); // DATA
        
        if (editsOpCode != FSEditLogOpCodes.OP_INVALID && 
            LayoutVersion.supports(Feature.EDITS_CHESKUM, editsVersion)) {
          v.visitInt(EditsElement.CHECKSUM);
        }
        v.leaveEnclosingElement(); // RECORD
      } while(editsOpCode != FSEditLogOpCodes.OP_INVALID);

      v.leaveEnclosingElement(); // EDITS
      v.finish();
    } catch(IOException e) {
      // Tell the visitor to clean up, then re-throw the exception
      v.finishAbnormally();
      throw e;
    }
  }
}
