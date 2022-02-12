/**
 * Copyright (c) 2022 Rohan Kumar and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package me.rohank05.beat.paginator;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;

public class Paginator {
    public final ArrayList<MessageEmbed> embeds;
    public Integer page;
    public final Long userId;


    public Paginator(ArrayList<MessageEmbed> embeds, Integer page, Long userId) {
        this.embeds = embeds;
        this.page = page;
        this.userId = userId;
    }

    public void increasePage() {
        page++;
    }

    public void decreasePage() {
        page++;
    }
}
