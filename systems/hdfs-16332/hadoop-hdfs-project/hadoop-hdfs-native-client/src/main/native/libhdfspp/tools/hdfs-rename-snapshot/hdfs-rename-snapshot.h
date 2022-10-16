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

#ifndef LIBHDFSPP_TOOLS_HDFS_RENAME_SNAPSHOT
#define LIBHDFSPP_TOOLS_HDFS_RENAME_SNAPSHOT

#include <string>

#include <boost/program_options.hpp>

#include "hdfs-tool.h"

namespace hdfs::tools {
/**
 * {@class RenameSnapshot} is an {@class HdfsTool} that facilitates the
 * renaming of the snapshots.
 */
class RenameSnapshot : public HdfsTool {
public:
  /**
   * {@inheritdoc}
   */
  RenameSnapshot(int argc, char **argv);

  // Abiding to the Rule of 5
  RenameSnapshot(const RenameSnapshot &) = default;
  RenameSnapshot(RenameSnapshot &&) = default;
  RenameSnapshot &operator=(const RenameSnapshot &) = delete;
  RenameSnapshot &operator=(RenameSnapshot &&) = delete;
  ~RenameSnapshot() override = default;

  /**
   * {@inheritdoc}
   */
  [[nodiscard]] std::string GetDescription() const override;

  /**
   * {@inheritdoc}
   */
  [[nodiscard]] bool Do() override;

protected:
  /**
   * {@inheritdoc}
   */
  [[nodiscard]] bool Initialize() override;

  /**
   * {@inheritdoc}
   */
  [[nodiscard]] bool ValidateConstraints() const override;

  /**
   * {@inheritdoc}
   */
  [[nodiscard]] bool HandleHelp() const override;

  /**
   * Handle the arguments that are passed to this tool.
   *
   * @param path The path to the directory that is snapshot-able.
   * @param old_name The name of the current/older snapshot.
   * @param new_name The new name for the old snapshot.
   *
   * @return A boolean indicating the result of this operation.
   */
  [[nodiscard]] virtual bool HandleSnapshot(const std::string &path,
                                            const std::string &old_name,
                                            const std::string &new_name) const;

private:
  /**
   * A boost data-structure containing the description of positional arguments
   * passed to the command-line.
   */
  po::positional_options_description pos_opt_desc_;
};
} // namespace hdfs::tools

#endif
